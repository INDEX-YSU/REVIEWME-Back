package club.index_ysu.reviewme.domain.post;

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
import club.index_ysu.reviewme.domain.post.dto.*;
import java.util.List;

/** 게시글 HTTP 요청의 검증 및 응답을 담당합니다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "게시글")
public class PostController {
    private final PostService service;
    /** 게시글 목록 조회합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @GetMapping()
    @Operation(summary = "게시글 목록 조회")
    @ApiResponse(responseCode = "200", description = "성공")

    public club.index_ysu.reviewme.global.api.ApiResponse<List<PostData>> list(@AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.list(userId));
    }

    /** 게시글 상세 조회합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<PostData> read(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.read(id, userId));
    }

    /** 게시글 조회수 증가합니다.
     * @param id 대상 리소스 ID
     * @return 처리 결과 */
    @PostMapping("/{id}/views")
    @Operation(summary = "게시글 조회수 증가")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<Long> view(@PathVariable Long id) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.view(id));
    }

    /** 게시글 작성합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PostMapping()
    @Operation(summary = "게시글 작성", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<club.index_ysu.reviewme.global.api.ApiResponse<PostCreateResponse>> create(@Valid @RequestBody PostCreateRequest input, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(club.index_ysu.reviewme.global.api.ApiResponse.success(service.createPost(input, userId)));
    }

    /** 게시글 수정합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<PostData> update(@PathVariable Long id, @Valid @RequestBody PostCreateRequest input, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.update(id, userId, input));
    }

    /** 게시글 삭제합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 본문 없는 성공 응답 */
    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    /** 게시글 추천합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @PutMapping("/{id}/like")
    @Operation(summary = "게시글 추천", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<PostData.Like> like(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.setLike(id, userId, true));
    }

    /** 게시글 추천 취소합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @DeleteMapping("/{id}/like")
    @Operation(summary = "게시글 추천 취소", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<PostData.Like> unlike(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.setLike(id, userId, false));
    }

    /** 댓글 목록 조회합니다.
     * @param id 대상 리소스 ID
     * @return 처리 결과 */
    @GetMapping("/{id}/comments")
    @Operation(summary = "댓글 목록 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<List<PostData.Reply>> comments(@PathVariable Long id) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.replies(id));
    }

    /** 댓글 작성합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PostMapping("/{id}/comments")
    @Operation(summary = "댓글 작성", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<club.index_ysu.reviewme.global.api.ApiResponse<PostData.Reply>> comment(@PathVariable Long id, @Valid @RequestBody PostData.ReplyInput input, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(club.index_ysu.reviewme.global.api.ApiResponse.success(service.reply(id, userId, input.content())));
    }

    /** 댓글 수정합니다.
     * @param id 대상 리소스 ID
     * @param commentId 댓글 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PutMapping("/{id}/comments/{commentId}")
    @Operation(summary = "댓글 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<PostData.Reply> editComment(@PathVariable Long id, @PathVariable Long commentId, @Valid @RequestBody PostData.ReplyInput input, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.editReply(id, commentId, userId, input.content()));
    }

    /** 댓글 삭제합니다.
     * @param id 대상 리소스 ID
     * @param commentId 댓글 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 본문 없는 성공 응답 */
    @DeleteMapping("/{id}/comments/{commentId}")
    @Operation(summary = "댓글 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @PathVariable Long commentId, @AuthenticationPrincipal Long userId) {
        service.deleteReply(id, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
