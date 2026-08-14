package club.index_ysu.reviewme.domain.post.dto;

/**
 * 게시글 생성 응답 DTO
 * 생성된 게시글의 ID를 틀라이언트에게 반환한다.
 */

public record PostCreateResponse(
        Long postId
) {
}
