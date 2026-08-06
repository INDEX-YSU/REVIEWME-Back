package club.index_ysu.reviewme.auth;

import club.index_ysu.reviewme.auth.dto.AuthResponses;
import club.index_ysu.reviewme.auth.dto.LoginRequest;
import club.index_ysu.reviewme.auth.dto.SignupRequest;
import club.index_ysu.reviewme.domain.auth.RefreshToken;
import club.index_ysu.reviewme.domain.auth.RefreshTokenRepository;
import club.index_ysu.reviewme.domain.user.*;
import club.index_ysu.reviewme.global.error.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${app.jwt.refresh-token-seconds}") long refreshTokenSeconds) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    @Transactional
    public AuthResponses.Signup signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }
        User user = User.builder().email(email).password(passwordEncoder.encode(request.password())).build();
        user.setProfile(UserProfile.builder().nickname(request.nickname().trim()).build());
        try {
            return AuthResponses.Signup.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new AuthException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }
    }

    @Transactional
    public TokenResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(this::invalidCredentials);
        if (!user.isActive() || user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResult refresh(String rawRefreshToken) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(this::invalidRefreshToken);
        if (!stored.isUsable(now) || !stored.getUser().isActive()) throw invalidRefreshToken();
        stored.revoke(now);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> token.revoke(LocalDateTime.now(ZoneOffset.UTC)));
    }

    private TokenResult issueTokens(User user) {
        String rawRefreshToken = newRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(refreshTokenSeconds);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user).tokenHash(hash(rawRefreshToken)).expiresAt(expiresAt).build());
        String accessToken = jwtService.createAccessToken(user);
        AuthResponses.Login response = new AuthResponses.Login(accessToken, jwtService.getAccessTokenSeconds(),
                AuthResponses.UserSummary.from(user));
        return new TokenResult(response, rawRefreshToken);
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) throw invalidRefreshToken();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private AuthException invalidCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    private AuthException invalidRefreshToken() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh Token이 유효하지 않습니다.");
    }

    public record TokenResult(AuthResponses.Login response, String refreshToken) {}
}
