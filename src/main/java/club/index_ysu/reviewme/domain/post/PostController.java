package club.index_ysu.reviewme.domain.post;


import club.index_ysu.reviewme.domain.post.dto.PostCreateRequest;
import club.index_ysu.reviewme.domain.post.dto.PostCreateResponse;
import club.index_ysu.reviewme.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시글 관련 HTTP 요청을 처리하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    /**
     * 게시글 생성
     *
     * @param request 게시글 생성 요청 정보
     * @param authentication 현재 로그인한 사용자 인증 정보
     * @return 생성된 게시글 정보
     */

    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            Authentication authentication
    ){
        //JWT 인증과정에서 principal 에 저장된 사용자 ID를 가져옴
        Long userId = (Long) authentication.getPrincipal();

        //게시글 생성 서비스 호출
        PostCreateResponse response = postService.createPost(request,userId);

        // 게시글 생성 성공 시 HTTP 201 created 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
