package club.index_ysu.reviewme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 회원가입 요청")
public record SignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(example = "user@example.com") String email,
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
                message = "비밀번호는 8~64자의 영문, 숫자, 특수문자를 포함해야 합니다.")
        @Schema(example = "P@ssw0rd123", minLength = 8, maxLength = 64) String password,
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 100, message = "닉네임은 100자 이하여야 합니다.")
        @Schema(example = "ivor", maxLength = 100) String nickname
) {}
