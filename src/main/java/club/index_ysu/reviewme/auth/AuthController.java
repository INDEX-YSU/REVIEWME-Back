package club.index_ysu.reviewme.auth;

import club.index_ysu.reviewme.auth.dto.AuthResponses;
import club.index_ysu.reviewme.auth.dto.LoginRequest;
import club.index_ysu.reviewme.auth.dto.SignupRequest;
import club.index_ysu.reviewme.global.api.ApiResponse;
import club.index_ysu.reviewme.global.api.ErrorResponse;
import club.index_ysu.reviewme.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "이메일 회원가입, 로그인, JWT 재발급 및 로그아웃")
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";
    private final AuthService authService;
    private final long refreshTokenSeconds;
    private final boolean secureCookie;

    public AuthController(AuthService authService,
                          @Value("${app.jwt.refresh-token-seconds}") long refreshTokenSeconds,
                          @Value("${app.auth.refresh-cookie-secure}") boolean secureCookie) {
        this.authService = authService;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/signup")
    @Operation(summary = "이메일 회원가입", description = "이메일, 비밀번호와 닉네임으로 일반 회원을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponses.SignupSuccess.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 또는 비밀번호 정책 위반",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"비밀번호는 8~64자의 영문, 숫자, 특수문자를 포함해야 합니다.\"}}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"EMAIL_ALREADY_EXISTS\",\"message\":\"이미 가입된 이메일입니다.\"}}")))
    })
    ResponseEntity<ApiResponse<AuthResponses.Signup>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.signup(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인",
            description = "Access Token을 응답하고 Refresh Token은 HttpOnly 쿠키로 발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    headers = @Header(name = "Set-Cookie", description = "HttpOnly Refresh Token 쿠키",
                            schema = @Schema(type = "string", example = "refreshToken={token}; Path=/api/v1/auth; HttpOnly; SameSite=Strict")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponses.LoginSuccess.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 형식",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"이메일 또는 비밀번호가 올바르지 않습니다.\"}}")))
    })
    ResponseEntity<ApiResponse<AuthResponses.Login>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.TokenResult result = authService.login(request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
                .body(ApiResponse.success(result.response()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 재발급",
            description = "HttpOnly Refresh Token 쿠키를 검증하고 두 토큰을 회전합니다.",
            security = @SecurityRequirement(name = OpenApiConfig.REFRESH_COOKIE))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 및 Rotation 성공",
                    headers = @Header(name = "Set-Cookie", description = "회전된 새 HttpOnly Refresh Token 쿠키",
                            schema = @Schema(type = "string", example = "refreshToken={new-token}; Path=/api/v1/auth; HttpOnly; SameSite=Strict")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponses.RefreshSuccess.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token 누락, 만료, 폐기 또는 재사용",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REFRESH_TOKEN\",\"message\":\"Refresh Token이 유효하지 않습니다.\"}}")))
    })
    ResponseEntity<ApiResponse<AuthResponses.Refresh>> refresh(HttpServletRequest request) {
        AuthService.TokenResult result = authService.refresh(readRefreshToken(request));
        var response = new AuthResponses.Refresh(result.response().accessToken(), result.response().expiresIn());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃",
            description = "Bearer Access Token 인증 후 현재 Refresh Token을 폐기하고 쿠키를 만료시킵니다.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    headers = @Header(name = "Set-Cookie", description = "즉시 만료된 Refresh Token 쿠키",
                            schema = @Schema(type = "string", example = "refreshToken=; Path=/api/v1/auth; Max-Age=0; HttpOnly; SameSite=Strict")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponses.LogoutSuccess.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Access Token 누락, 만료 또는 변조",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}}")))
    })
    ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(readRefreshTokenOrNull(request));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(null, "로그아웃 되었습니다."));
    }

    private String readRefreshToken(HttpServletRequest request) {
        String token = readRefreshTokenOrNull(request);
        if (token == null) return "";
        return token;
    }

    private String readRefreshTokenOrNull(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(cookie -> REFRESH_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }

    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token).httpOnly(true).secure(secureCookie)
                .sameSite("Strict").path("/api/v1/auth").maxAge(Duration.ofSeconds(refreshTokenSeconds)).build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(secureCookie)
                .sameSite("Strict").path("/api/v1/auth").maxAge(Duration.ZERO).build();
    }
}
