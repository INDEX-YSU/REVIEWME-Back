package club.index_ysu.reviewme.config;

import club.index_ysu.reviewme.auth.AuthService;
import club.index_ysu.reviewme.auth.dto.SignupRequest;
import club.index_ysu.reviewme.domain.chat.ChatData;
import club.index_ysu.reviewme.domain.chat.ChatService;
import club.index_ysu.reviewme.domain.post.PostService;
import club.index_ysu.reviewme.domain.post.dto.PostCreateRequest;
import club.index_ysu.reviewme.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 로컬 데모 DB에만 시연에 사용할 가상 계정과 게시판·채팅 데이터를 제공합니다. */
@Component
@Profile("local & demo")
@RequiredArgsConstructor
public class DemoData implements ApplicationRunner {
    private static final String AUTHOR = "author@reviewme.demo";
    private static final String REVIEWER = "reviewer@reviewme.demo";
    private static final String DEMO_PASSWORD = "Reviewme!2026";

    private final AuthService auth;
    private final UserRepository users;
    private final PostService posts;
    private final ChatService chat;

    /** 최초 실행에서만 가상 데이터를 한 트랜잭션으로 저장합니다.
     * @param args 애플리케이션 실행 인자 */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByEmail(AUTHOR)) return;

        auth.signup(new SignupRequest(AUTHOR, DEMO_PASSWORD, "서윤 · 작성자"));
        auth.signup(new SignupRequest(REVIEWER, DEMO_PASSWORD, "민준 · 리뷰어"));
        var author = users.findByEmail(AUTHOR).orElseThrow();
        var reviewer = users.findByEmail(REVIEWER).orElseThrow();
        author.getProfile().update("서윤 · 작성자", "작은 제품을 만들며 프런트엔드를 공부하고 있어요.", "INDEX", "TypeScript\nReact");
        reviewer.getProfile().update("민준 · 리뷰어", "읽기 쉬운 API와 테스트를 좋아합니다.", "INDEX", "Java\nSpring Boot");

        sample(reviewer.getId(), "정렬 옵션은 URL에 저장하는 게 좋을까요?", "프론트엔드", """
                게시판에서 정렬과 검색어를 새로고침 후에도 복원하려고 합니다.

                URL 검색 매개변수에 상태를 두면 공유가 쉬워 보이는데, 입력 중 브라우저 기록을 어떻게 다루시나요?
                """);
        sample(author.getId(), "첫 팀 프로젝트 README에서 빠진 내용이 있을까요?", "취업/포트폴리오", """
                구현 기능과 설치 방법을 정리했습니다. 기술 선택과 실패한 시도도 넣으면 흐름이 길어질까 고민이에요.

                처음 저장소를 여는 입장에서 어떤 정보가 먼저 보이면 좋을지 궁금합니다.
                """);
        sample(reviewer.getId(), "Spring 테스트에서 로그인 준비 코드 줄이기", "백엔드", """
                회원가입과 로그인 요청을 공통 헬퍼로 옮겼습니다. 각 테스트는 실제 인증과 권한 흐름을 그대로 거칩니다.

                테스트 헬퍼가 권한 시나리오를 가리지 않으려면 어디까지 재사용하는 게 좋을까요?
                """);
        sample(author.getId(), "마크다운 입력과 미리보기 접근성 점검", "프론트엔드", """
                입력과 미리보기 탭을 분리했습니다. 키보드만으로 내용을 편집하고 오류 위치를 찾을 수 있는지 리뷰를 부탁드립니다.
                """);
        sample(reviewer.getId(), "사이드 프로젝트 리뷰를 어떤 순서로 진행하시나요?", "사이드프로젝트", """
                스터디에서는 기능 흐름부터 보고, 변경 범위와 예외 처리를 다음으로 확인합니다. 리뷰 시간을 줄이기 위해 체크리스트를 만들고 있습니다.
                """);
        long first = sample(author.getId(), "검색 입력마다 API를 호출하지 않게 바꿨어요", "프론트엔드", """
                ## 검토 요청

                검색어 입력 흐름을 250ms 지연 처리로 바꿨습니다. 키보드 입력이 빠를 때 마지막 검색어만 서버로 보내는 흐름을 확인해 주세요.

                ```tsx
                useEffect(() => {
                  const timer = setTimeout(() => setQuery(input.trim()), 250);
                  return () => clearTimeout(timer);
                }, [input]);
                ```

                - 검색창을 비우면 전체 목록을 보여 줍니다.
                - 검색 요청이 겹칠 때 이전 응답을 버리는 방법이 궁금합니다.
                """);
        posts.reply(first, reviewer.getId(), "이전 요청에 AbortController를 연결하고, 입력이 바뀔 때 정리 함수에서 중단하면 이전 응답이 화면을 덮는 문제를 줄일 수 있어요.");
        posts.reply(first, author.getId(), "감사합니다. 검색어 지우기와 느린 응답이 뒤늦게 도착하는 경우를 같이 확인하겠습니다.");
        posts.setLike(first, reviewer.getId(), true);

        ChatData.Room lobby = chat.create(reviewer.getId(), new ChatData.RoomInput(
                "리뷰 라운지", "리뷰 주제와 프로젝트 고민을 편하게 나누는 공개방입니다.", "자유", false, true));
        chat.send(lobby.id(), reviewer.getId(), new ChatData.MessageInput("리뷰 라운지에 오신 걸 환영합니다. 오늘 검토받고 싶은 주제를 적어 주세요."));
        chat.send(lobby.id(), author.getId(), new ChatData.MessageInput("검색 입력 최적화 글을 올렸어요. 요청이 겹칠 때 처리 방식이 궁금합니다."));
        ChatData.Room privateRoom = chat.create(author.getId(), new ChatData.RoomInput(
                "INDEX 리뷰 스터디", "초대받은 구성원이 이번 주 리뷰 주제를 논의하는 공간입니다.", "사이드프로젝트", true, false));
        chat.send(privateRoom.id(), author.getId(), new ChatData.MessageInput("초대 링크를 받은 분은 여기에 인사해 주세요."));
    }

    /** @param authorId 가상 작성자 ID
     * @param title 예시 글 제목
     * @param category 예시 글의 카테고리
     * @param content 예시 마크다운 본문
     * @return 예시 글에 할당된 ID */
    private long sample(Long authorId, String title, String category, String content) {
        return posts.createPost(new PostCreateRequest(title, content, category), authorId).postId();
    }
}
