package club.index_ysu.reviewme.domain.chat;

import club.index_ysu.reviewme.global.error.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** 채팅 저장소의 접근 권한을 검사하고 채팅방, 메시지 및 초대를 트랜잭션 단위로 처리합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private static final String ROOMS = """
            select r.*, p.nickname, (select count(*) from CHAT_ROOM_MEMBER m where m.room_id = r.id) participants
            from CHAT_ROOM r join USER_PROFILE p on p.user_id = r.host_id
            """;
    private static final String MESSAGES = """
            select m.*, p.nickname from CHAT_MESSAGE m join USER_PROFILE p on p.user_id = m.user_id
            """;
    private static final RowMapper<ChatData.Room> ROOM_ROW = (row, index) -> new ChatData.Room(
            row.getLong("id"), row.getString("title"), row.getString("description"), row.getString("category"),
            "PRIVATE".equals(row.getString("type")), row.getBoolean("allow_guest"), row.getLong("participants"),
            "u_" + row.getLong("host_id"), row.getString("nickname"));
    private static final RowMapper<ChatData.Message> MESSAGE_ROW = (row, index) -> new ChatData.Message(
            row.getLong("id"), "u_" + row.getLong("user_id"), row.getString("nickname"),
            row.getBoolean("is_deleted") ? "" : row.getString("message"),
            row.getObject("created_at", LocalDateTime.class), row.getBoolean("is_deleted"));

    private final JdbcTemplate db;

    /** @param userId 요청 계정 또는 null
     * @return 요청자가 직접 열 수 있는 채팅방 목록 */
    @Transactional(readOnly = true)
    public List<ChatData.Room> list(Long userId) {
        return db.query(ROOMS + """
                where (r.type = 'PUBLIC' and (r.allow_guest = true or ? = true))
                  or exists (select 1 from CHAT_ROOM_MEMBER m where m.room_id = r.id and m.user_id = ?)
                order by r.id desc
                """, ROOM_ROW, userId != null, userId);
    }

    /** @param id 채팅방 ID
     * @param userId 요청 계정 또는 null
     * @return 접근 권한을 확인한 채팅방
     * @throws AuthException 채팅방이 없거나 접근이 거절된 경우 */
    @Transactional(readOnly = true)
    public ChatData.Room read(Long id, Long userId) {
        ChatData.Room room = db.query(ROOMS + " where r.id = ?", ROOM_ROW, id).stream().findFirst()
                .orElseThrow(ChatService::missingRoom);
        if (room.isPrivate() && !isMember(id, userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "PRIVATE_ROOM", "초대받은 사용자만 입장할 수 있습니다.");
        }
        if (!room.isPrivate() && !room.allowGuest() && userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인 후 입장할 수 있습니다.");
        }
        return room;
    }

    /** @param userId 방장 계정 ID
     * @param input 채팅방의 제목, 설명과 공개 범위
     * @return 방장 가입까지 완료된 채팅방 */
    public ChatData.Room create(Long userId, ChatData.RoomInput input) {
        boolean privateRoom = Boolean.TRUE.equals(input.isPrivate());
        long id = insert("""
                insert into CHAT_ROOM (title, description, category, type, allow_guest, host_id)
                values (?, ?, ?, ?, ?, ?)
                """, input.title().strip(), input.description().strip(), input.category(),
                privateRoom ? "PRIVATE" : "PUBLIC", !privateRoom && Boolean.TRUE.equals(input.allowGuest()), userId);
        addMember(id, userId);
        return read(id, userId);
    }

    /** @param id 채팅방 ID
     * @param userId 요청 계정 또는 null
     * @param before 이 ID보다 오래된 메시지만 반환하는 선택 커서
     * @return 최근 메시지 최대 50개를 시간 오름차순으로 반환합니다. */
    @Transactional(readOnly = true)
    public List<ChatData.Message> messages(Long id, Long userId, Long before) {
        read(id, userId);
        var rows = before == null
                ? db.query(MESSAGES + " where m.room_id = ? order by m.id desc limit 50", MESSAGE_ROW, id)
                : db.query(MESSAGES + " where m.room_id = ? and m.id < ? order by m.id desc limit 50", MESSAGE_ROW, id, before);
        var ordered = new ArrayList<>(rows);
        Collections.reverse(ordered);
        return ordered;
    }

    /** @param id 채팅방 ID
     * @param userId 메시지 작성자 계정 ID
     * @param input 저장할 본문
     * @return 새 메시지
     * @throws AuthException 방에 접근할 수 없는 경우 */
    public ChatData.Message send(Long id, Long userId, ChatData.MessageInput input) {
        lockRoom(id);
        read(id, userId);
        if (!isMember(id, userId)) addMember(id, userId);
        long messageId = insert("insert into CHAT_MESSAGE (room_id, user_id, message) values (?, ?, ?)",
                id, userId, input.content().strip());
        return db.queryForObject(MESSAGES + " where m.id = ?", MESSAGE_ROW, messageId);
    }

    /** @param id 채팅방 ID
     * @param messageId 메시지 ID
     * @param userId 삭제 요청자 ID
     * @throws AuthException 대상 메시지가 없거나 작성자가 다른 경우 */
    public void deleteMessage(Long id, Long messageId, Long userId) {
        read(id, userId);
        Long owner = db.query("select user_id from CHAT_MESSAGE where id = ? and room_id = ?",
                (row, index) -> row.getLong(1), messageId, id).stream().findFirst()
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."));
        if (!Objects.equals(userId, owner)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "NOT_OWNER", "작성한 메시지만 삭제할 수 있습니다.");
        }
        db.update("update CHAT_MESSAGE set is_deleted = true where id = ?", messageId);
    }

    /** @param id 초대할 비공개 채팅방 ID
     * @param userId 초대 생성 요청자 ID
     * @param input 유효 기간
     * @return 만료형 일회용 초대 링크의 토큰
     * @throws AuthException 비공개 방의 방장이 아닌 경우 */
    public ChatData.Invite invite(Long id, Long userId, ChatData.InviteInput input) {
        ChatData.Room room = read(id, userId);
        if (!room.isPrivate() || !room.ownerId().equals("u_" + userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "NOT_HOST", "비공개 방의 방장만 초대할 수 있습니다.");
        }
        String token = UUID.randomUUID().toString();
        var expires = LocalDateTime.now().plusMinutes(input.minutes());
        db.update("insert into CHAT_INVITE_LINK (room_id, invite_code, expires_at) values (?, ?, ?)", id, token, expires);
        return new ChatData.Invite(token, expires);
    }

    /** 동일 방의 가입을 잠금으로 직렬화해 한 초대가 중복 소진되지 않게 합니다.
     * @param token 초대 코드
     * @param userId 초대를 수락하는 계정 ID
     * @return 가입이 끝난 방
     * @throws AuthException 초대가 없거나 만료되었거나 이미 사용된 경우 */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ChatData.Room accept(String token, Long userId) {
        Long id = db.query("select room_id from CHAT_INVITE_LINK where invite_code = ?",
                (row, index) -> row.getLong(1), token).stream().findFirst()
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "초대를 찾을 수 없습니다."));
        lockRoom(id);
        if (isMember(id, userId)) return read(id, userId);
        int consumed = db.update("""
                update CHAT_INVITE_LINK set current_uses = current_uses + 1
                where invite_code = ? and expires_at > CURRENT_TIMESTAMP and current_uses < max_uses
                """, token);
        if (consumed == 0) {
            throw new AuthException(HttpStatus.GONE, "INVITE_EXPIRED", "만료되었거나 이미 사용된 초대입니다.");
        }
        addMember(id, userId);
        return read(id, userId);
    }

    /** @return 채팅방이 없을 때 사용하는 공통 오류 */
    private static AuthException missingRoom() {
        return new AuthException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
    }

    /** @param id 채팅방 ID
     * @throws AuthException 존재하지 않는 방인 경우
     * 해당 방의 회원 또는 메시지를 쓰는 트랜잭션이 끝날 때까지 잠금을 유지합니다. */
    private void lockRoom(Long id) {
        db.query("select id from CHAT_ROOM where id = ? for update", (row, index) -> row.getLong(1), id)
                .stream().findFirst().orElseThrow(ChatService::missingRoom);
    }

    /** @param id 채팅방 ID
     * @param userId 확인할 계정 또는 null
     * @return 가입이 확인되면 true */
    private boolean isMember(Long id, Long userId) {
        return userId != null && db.queryForObject("select count(*) from CHAT_ROOM_MEMBER where room_id = ? and user_id = ?",
                Long.class, id, userId) > 0;
    }

    /** @param id 가입할 방 ID
     * @param userId 가입할 계정 ID */
    private void addMember(Long id, Long userId) {
        db.update("insert into CHAT_ROOM_MEMBER (room_id, user_id) values (?, ?)", id, userId);
    }

    /** @param sql 키를 생성하는 INSERT SQL
     * @param values 위치 매개변수
     * @return DB가 생성한 행 ID */
    private long insert(String sql, Object... values) {
        var key = new GeneratedKeyHolder();
        db.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            for (int index = 0; index < values.length; index++) statement.setObject(index + 1, values[index]);
            return statement;
        }, key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }
}
