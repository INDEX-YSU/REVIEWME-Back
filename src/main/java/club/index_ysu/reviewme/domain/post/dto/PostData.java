package club.index_ysu.reviewme.domain.post.dto;

import club.index_ysu.reviewme.domain.comment.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** 작성자 표시 정보와 현재 사용자의 추천 상태를 담은 게시글 응답입니다. */
public record PostData(Long id, String title, String category, String content, String author,
                       @Schema(example = "u_1") String authorId, LocalDateTime createdAt,
                       long likes, long comments, long views, boolean likedByMe) {
    /** 일반 텍스트 댓글 입력입니다. */
    public record ReplyInput(
            @NotBlank(message = "댓글을 입력해 주세요.")
            @Size(max = 2000, message = "댓글은 2,000자까지 입력할 수 있습니다.")
            @Schema(example = "예외 처리 흐름을 보완하면 좋겠습니다.")
            String content) {}

    /** 삭제되지 않은 댓글의 본문과 작성자 표시 정보입니다. */
    public record Reply(Long id, Long postId, String author, String authorId, String content, LocalDateTime createdAt) {
        /** @param comment 응답에 노출할 댓글
         * @return 보안을 위해 작성자의 공개 필드만 포함한 댓글 */
        public static Reply from(Comment comment) {
            var user = comment.getUser();
            return new Reply(comment.getId(), comment.getPost().getId(), user.getProfile().getNickname(),
                    "u_" + user.getId(), comment.getDisplayContent(), comment.getCreatedAt());
        }
    }

    /** 추천 버튼의 최종 상태와 게시글의 전체 추천 수입니다. */
    public record Like(long likes, boolean likedByMe) {}
}
