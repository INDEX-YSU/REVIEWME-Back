package club.index_ysu.reviewme.global.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "공통 실패 응답")
public record ErrorResponse(
        @Schema(example = "false") boolean success,
        ErrorDetail error) {
    public ErrorResponse(String code, String message) {
        this(false, new ErrorDetail(code, message));
    }

    @Schema(name = "ErrorDetail")
    public record ErrorDetail(
            @Schema(example = "INVALID_REQUEST") String code,
            @Schema(example = "요청 값이 올바르지 않습니다.") String message) {}
}
