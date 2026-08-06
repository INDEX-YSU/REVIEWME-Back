package club.index_ysu.reviewme.auth.dto;

import club.index_ysu.reviewme.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponses", description = "인증 API 응답 데이터")
public final class AuthResponses {
    private AuthResponses() {}

    @Schema(name = "SignupData", description = "회원가입 결과")
    public record Signup(
            @Schema(example = "u_1029") String userId,
            @Schema(example = "user@example.com") String email,
            @Schema(example = "ivor") String nickname,
            @Schema(example = "USER", allowableValues = {"USER", "ADMIN"}) String role) {
        public static Signup from(User user) {
            return new Signup(formatUserId(user), user.getEmail(), user.getProfile().getNickname(), user.getRole().name());
        }
    }

    @Schema(name = "AuthUserSummary", description = "로그인 사용자 요약")
    public record UserSummary(
            @Schema(example = "u_1029") String userId,
            @Schema(example = "ivor") String nickname,
            @Schema(example = "USER", allowableValues = {"USER", "ADMIN"}) String role) {
        public static UserSummary from(User user) {
            return new UserSummary(formatUserId(user), user.getProfile().getNickname(), user.getRole().name());
        }
    }

    @Schema(name = "LoginData", description = "로그인 결과")
    public record Login(
            @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
            @Schema(description = "Access Token 만료 시간(초)", example = "3600") long expiresIn,
            UserSummary user) {}

    @Schema(name = "TokenRefreshData", description = "Access Token 재발급 결과")
    public record Refresh(
            @Schema(description = "새 JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
            @Schema(description = "Access Token 만료 시간(초)", example = "3600") long expiresIn) {}

    @Schema(name = "SignupSuccessResponse")
    public record SignupSuccess(boolean success, Signup data) {}

    @Schema(name = "LoginSuccessResponse")
    public record LoginSuccess(boolean success, Login data) {}

    @Schema(name = "TokenRefreshSuccessResponse")
    public record RefreshSuccess(boolean success, Refresh data) {}

    @Schema(name = "LogoutSuccessResponse")
    public record LogoutSuccess(boolean success, @Schema(example = "로그아웃 되었습니다.") String message) {}

    private static String formatUserId(User user) {
        return "u_" + user.getId();
    }
}
