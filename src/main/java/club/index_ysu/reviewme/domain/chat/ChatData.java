package club.index_ysu.reviewme.domain.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** 채팅방 생성, 메시지 전송 및 초대에 사용하는 HTTP 계약입니다. */
public final class ChatData {
    private ChatData() {}

    /** 방의 공개 범위와 비회원 열람 가능 여부입니다. 생략한 플래그는 false로 처리합니다. */
    public record RoomInput(
            @NotBlank @Size(max = 120) @Schema(example = "리뷰 스터디") String title,
            @NotNull @Size(max = 500) String description,
            @NotBlank @Pattern(regexp = "프론트엔드|백엔드|AI|취업/포트폴리오|사이드프로젝트|자유") String category,
            Boolean isPrivate, Boolean allowGuest) {}

    /** 열람 권한이 있는 방의 표시 정보와 가입한 사용자 수입니다. */
    public record Room(Long id, String title, String description, String category, boolean isPrivate,
                       boolean allowGuest, long participants, String ownerId, String ownerNickname) {}

    /** 최대 2,000자의 일반 텍스트 메시지입니다. */
    public record MessageInput(@NotBlank @Size(max = 2000) @Schema(example = "리뷰 감사합니다.") String content) {}

    /** 저장된 메시지입니다. 삭제 시 내용은 응답에서 비웁니다. */
    public record Message(Long id, String senderId, String senderNickname, String content,
                          LocalDateTime createdAt, boolean isDeleted) {}

    /** 1분부터 7일까지의 초대 유효 시간입니다. */
    public record InviteInput(@NotNull @Min(1) @Max(10080) @Schema(example = "60") Integer minutes) {}

    /** 새 참가자 한 명이 사용할 수 있는 만료형 초대 정보입니다. */
    public record Invite(String token, LocalDateTime expiresAt) {}
}
