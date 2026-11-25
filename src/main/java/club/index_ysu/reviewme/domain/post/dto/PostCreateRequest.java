package club.index_ysu.reviewme.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 게시글 작성과 편집에 필요한 제목, 마크다운 본문 및 카테고리입니다. */
public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자까지 입력할 수 있습니다.")
        @Schema(example = "코드 리뷰를 부탁드립니다")
        String title,
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "본문은 10,000자까지 입력할 수 있습니다.")
        @Schema(example = "## 검토하고 싶은 내용")
        String content,
        @Pattern(regexp = "프론트엔드|백엔드|AI|취업/포트폴리오|사이드프로젝트|자유",
                message = "카테고리가 올바르지 않습니다.")
        @Schema(example = "백엔드", defaultValue = "자유")
        String category
) {
    /** @return 이전 클라이언트가 카테고리를 생략한 경우 자유 카테고리 */
    public String categoryOrDefault() {
        return category == null ? "자유" : category;
    }
}
