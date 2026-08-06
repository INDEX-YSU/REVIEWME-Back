package club.index_ysu.reviewme.auth;

import club.index_ysu.reviewme.auth.dto.LoginRequest;
import club.index_ysu.reviewme.auth.dto.SignupRequest;
import club.index_ysu.reviewme.global.error.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuthServiceIntegrationTest {
    @Autowired AuthService authService;

    @Test
    void signupAndLoginIssueTokens() {
        var signup = authService.signup(new SignupRequest("User@Example.com", "P@ssw0rd123", "ivor"));

        assertThat(signup.email()).isEqualTo("user@example.com");
        assertThat(signup.nickname()).isEqualTo("ivor");
        assertThat(signup.role()).isEqualTo("USER");

        var login = authService.login(new LoginRequest("user@example.com", "P@ssw0rd123"));
        assertThat(login.response().accessToken()).isNotBlank();
        assertThat(login.refreshToken()).isNotBlank();
        assertThat(login.response().expiresIn()).isEqualTo(3600);
    }

    @Test
    void refreshRotatesTokenAndRejectsOldToken() {
        authService.signup(new SignupRequest("rotate@example.com", "P@ssw0rd123", "rotate"));
        var login = authService.login(new LoginRequest("rotate@example.com", "P@ssw0rd123"));

        var refreshed = authService.refresh(login.refreshToken());

        assertThat(refreshed.refreshToken()).isNotEqualTo(login.refreshToken());
        assertThatThrownBy(() -> authService.refresh(login.refreshToken()))
                .isInstanceOf(AuthException.class)
                .extracting("code").isEqualTo("INVALID_REFRESH_TOKEN");
    }

    @Test
    void logoutRevokesRefreshToken() {
        authService.signup(new SignupRequest("logout@example.com", "P@ssw0rd123", "logout"));
        var login = authService.login(new LoginRequest("logout@example.com", "P@ssw0rd123"));

        authService.logout(login.refreshToken());

        assertThatThrownBy(() -> authService.refresh(login.refreshToken()))
                .isInstanceOf(AuthException.class)
                .extracting("code").isEqualTo("INVALID_REFRESH_TOKEN");
    }

    @Test
    void invalidPasswordReturnsGenericAuthenticationError() {
        authService.signup(new SignupRequest("invalid@example.com", "P@ssw0rd123", "invalid"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("invalid@example.com", "wrong-password")))
                .isInstanceOf(AuthException.class)
                .extracting("code").isEqualTo("INVALID_CREDENTIALS");
    }
}
