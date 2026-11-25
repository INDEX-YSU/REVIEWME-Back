package club.index_ysu.reviewme.domain.chat;

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
import java.util.List;

/** 채팅 HTTP 요청의 검증 및 응답을 담당합니다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "채팅")
public class ChatController {
    private final ChatService service;
    /** 접근 가능한 채팅방 목록 조회합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @GetMapping("/rooms")
    @Operation(summary = "접근 가능한 채팅방 목록 조회")
    @ApiResponse(responseCode = "200", description = "성공")

    public club.index_ysu.reviewme.global.api.ApiResponse<List<ChatData.Room>> list(@AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.list(userId));
    }

    /** 채팅방 조회합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @return 처리 결과 */
    @GetMapping("/rooms/{id}")
    @Operation(summary = "채팅방 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<ChatData.Room> read(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.read(id, userId));
    }

    /** 채팅방 생성합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PostMapping("/rooms")
    @Operation(summary = "채팅방 생성", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<club.index_ysu.reviewme.global.api.ApiResponse<ChatData.Room>> create(@Valid @RequestBody ChatData.RoomInput input, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(club.index_ysu.reviewme.global.api.ApiResponse.success(service.create(userId, input)));
    }

    /** 채팅 메시지 조회합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param before 이전 메시지를 조회할 기준 ID
     * @return 처리 결과 */
    @GetMapping("/rooms/{id}/messages")
    @Operation(summary = "채팅 메시지 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<List<ChatData.Message>> messages(@PathVariable Long id, @RequestParam(required = false) Long before, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.messages(id, userId, before));
    }

    /** 채팅 메시지 전송합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PostMapping("/rooms/{id}/messages")
    @Operation(summary = "채팅 메시지 전송", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<club.index_ysu.reviewme.global.api.ApiResponse<ChatData.Message>> send(@PathVariable Long id, @Valid @RequestBody ChatData.MessageInput input, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(club.index_ysu.reviewme.global.api.ApiResponse.success(service.send(id, userId, input)));
    }

    /** 내 채팅 메시지 삭제합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param messageId 삭제할 메시지 ID
     * @return 본문 없는 성공 응답 */
    @DeleteMapping("/rooms/{id}/messages/{messageId}")
    @Operation(summary = "내 채팅 메시지 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @PathVariable Long messageId, @AuthenticationPrincipal Long userId) {
        service.deleteMessage(id, messageId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 비공개 방 초대 링크 발급합니다.
     * @param id 대상 리소스 ID
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param input 검증할 요청 본문
     * @return 처리 결과 */
    @PostMapping("/rooms/{id}/invites")
    @Operation(summary = "비공개 방 초대 링크 발급", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public ResponseEntity<club.index_ysu.reviewme.global.api.ApiResponse<ChatData.Invite>> invite(@PathVariable Long id, @Valid @RequestBody ChatData.InviteInput input, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(club.index_ysu.reviewme.global.api.ApiResponse.success(service.invite(id, userId, input)));
    }

    /** 채팅방 초대 수락합니다.
     * @param userId 인증된 사용자 ID 또는 공개 조회의 경우 null
     * @param token 초대 링크의 코드
     * @return 처리 결과 */
    @PostMapping("/invites/{token}/accept")
    @Operation(summary = "채팅방 초대 수락", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    @ApiResponse(responseCode = "410", description = "초대 만료", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"success\":false,\"error\":{\"code\":\"INVITE_EXPIRED\",\"message\":\"요청을 처리할 수 없습니다.\"}}")))
    public club.index_ysu.reviewme.global.api.ApiResponse<ChatData.Room> accept(@PathVariable String token, @AuthenticationPrincipal Long userId) {
        return club.index_ysu.reviewme.global.api.ApiResponse.success(service.accept(token, userId));
    }
}
