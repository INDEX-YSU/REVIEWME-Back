package club.index_ysu.reviewme.domain.user;

import club.index_ysu.reviewme.global.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.*;
import java.util.Arrays;
import java.util.List;

/** 프로필 HTTP 요청의 검증 및 응답을 담당합니다. */
@RestController
@Transactional
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
@Tag(name = "프로필")
public class ProfileController {
    private final UserRepository users;
    /** 내 프로필 조회합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @GetMapping()
    @Operation(summary = "내 프로필 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<Profile> current(@AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(Profile.from(users.getReferenceById(userId)));
    }

    /** 내 프로필 수정합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PutMapping()
    @Operation(summary = "내 프로필 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<Profile> update(@Valid @RequestBody ProfileInput input, @AuthenticationPrincipal Long userId) {
        User user = users.getReferenceById(userId);
        user.getProfile().update(input.nickname().strip(), input.bio().strip(), input.affiliation().strip(),
                String.join("\n", input.techStack().stream().map(String::strip).distinct().toList()));
        return club.index_ysu.reviewme.global.api.ApiResponse.success(Profile.from(user));
    }

    /** 닉네임과 공개 프로필 정보입니다. 기술 이름의 줄바꿈은 저장 시 구분자로 사용합니다. */
    public record ProfileInput(
            @NotBlank @Size(max = 100) @Schema(example = "리뷰어") String nickname,
            @NotNull @Size(max = 2000) String bio,
            @NotNull @Size(max = 150) String affiliation,
            @NotNull @Size(max = 12) List<@NotBlank @Size(max = 40) @Pattern(regexp = "[^\\r\\n]+") String> techStack) {}

    /** 현재 계정의 식별자, 이메일 및 편집 가능한 프로필입니다. */
    public record Profile(String userId, String nickname, String email, String bio,
                          String affiliation, List<String> techStack, String profileImage) {
        /** @param user 조회할 계정
         * @return 빈 선택 필드를 정규화한 프로필 응답 */
        static Profile from(User user) {
            var profile = user.getProfile();
            var tech = profile.getTechStack();
            return new Profile("u_" + user.getId(), profile.getNickname(), user.getEmail(),
                    profile.getBio() == null ? "" : profile.getBio(),
                    profile.getAffiliation() == null ? "" : profile.getAffiliation(),
                    tech == null || tech.isBlank() ? List.of() : Arrays.asList(tech.split("\n")),
                    profile.getAvatarUrl() == null ? "" : profile.getAvatarUrl());
        }
    }
}
