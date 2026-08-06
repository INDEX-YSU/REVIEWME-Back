package club.index_ysu.reviewme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 로그인 요청")
public record LoginRequest(
        @NotBlank @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(example = "user@example.com") String email,
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Schema(example = "P@ssw0rd123") String password
) {}
