package club.index_ysu.reviewme.domain.post;

import club.index_ysu.reviewme.domain.post.dto.PostCreateRequest;
import club.index_ysu.reviewme.domain.post.dto.PostCreateResponse;
import club.index_ysu.reviewme.domain.user.User;
import club.index_ysu.reviewme.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {


    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     *
     * @param request 게시글 제목과 본문 정보
     * @param userId 게시글 작성자
     * @return 생성된 게시글의 ID
     */
    public PostCreateResponse createPost(PostCreateRequest request, Long userId){

        //작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow();

        //요청 받은 제목과 본문으로 게시글 객체를 생성
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .build();

        //생성한 게시글에 작성자를 연결
        post.assignUser(user);

        //게시글을 DB에 저장
        Post savedPost = postRepository.save(post);

        //저장된 게시글의 ID를 반환
        return new PostCreateResponse(savedPost.getId());
    }
}