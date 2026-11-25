package club.index_ysu.reviewme.domain.post;

import club.index_ysu.reviewme.domain.comment.Comment;
import club.index_ysu.reviewme.domain.comment.CommentRepository;
import club.index_ysu.reviewme.domain.post.dto.PostCreateRequest;
import club.index_ysu.reviewme.domain.post.dto.PostCreateResponse;
import club.index_ysu.reviewme.domain.post.dto.PostData;
import club.index_ysu.reviewme.domain.user.User;
import club.index_ysu.reviewme.domain.user.UserRepository;
import club.index_ysu.reviewme.global.error.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** 게시글, 댓글 및 추천을 같은 트랜잭션에서 조회하고 변경합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository posts;
    private final UserRepository users;
    private final CommentRepository comments;
    private final PostLikeRepository likes;

    /** @param userId 작성자 ID
     * @param request 게시글 입력값
     * @return 생성된 게시글 ID */
    public PostCreateResponse createPost(PostCreateRequest request, Long userId) {
        Post post = Post.builder().title(request.title().strip()).content(request.content())
                .category(request.categoryOrDefault()).build();
        post.assignUser(users.getReferenceById(userId));
        return new PostCreateResponse(posts.save(post).getId());
    }

    /** @param userId 로그인 사용자 ID 또는 null
     * @return 삭제되지 않은 게시글을 최신순으로 정렬한 목록 */
    @Transactional(readOnly = true)
    public List<PostData> list(Long userId) {
        return posts.findAllByDeletedAtIsNullOrderByIdDesc().stream()
                .map(post -> describe(post, userId)).toList();
    }

    /** @param id 게시글 ID
     * @param userId 로그인 사용자 ID 또는 null
     * @return 조회수를 포함한 게시글 */
    @Transactional(readOnly = true)
    public PostData read(Long id, Long userId) {
        return describe(requirePost(id), userId);
    }

    /** @param id 게시글 ID
     * @return 증가된 조회수 */
    public long view(Long id) {
        Post post = lockPost(id);
        post.increaseViewCount();
        return post.getViewCount();
    }

    /** @param id 게시글 ID
     * @param userId 수정 요청자 ID
     * @param request 새 제목, 내용, 카테고리
     * @return 수정된 게시글 */
    public PostData update(Long id, Long userId, PostCreateRequest request) {
        Post post = lockPost(id);
        requireOwner(post.getUser(), userId);
        post.update(request.title().strip(), request.content(), request.categoryOrDefault());
        return describe(post, userId);
    }

    /** @param id 게시글 ID
     * @param userId 삭제 요청자 ID */
    public void delete(Long id, Long userId) {
        Post post = lockPost(id);
        requireOwner(post.getUser(), userId);
        post.delete();
    }

    /** 같은 게시글의 추천 변경을 직렬화해 반복 요청을 안전하게 처리합니다.
     * @param id 게시글 ID
     * @param userId 추천 요청자 ID
     * @param liked 원하는 추천 상태
     * @return 변경 후 추천 상태와 총 수 */
    public PostData.Like setLike(Long id, Long userId, boolean liked) {
        Post post = lockPost(id);
        var existing = likes.findByPostIdAndUserId(id, userId);
        if (liked && existing.isEmpty()) {
            PostLike like = new PostLike(users.getReferenceById(userId));
            like.assignPost(post);
            likes.save(like);
        }
        if (!liked) existing.ifPresent(likes::delete);
        return new PostData.Like(likes.countByPostId(id), liked);
    }

    /** @param id 게시글 ID
     * @return 삭제되지 않은 댓글 목록 */
    @Transactional(readOnly = true)
    public List<PostData.Reply> replies(Long id) {
        requirePost(id);
        return comments.findAllByPostIdAndDeletedAtIsNullOrderByIdAsc(id).stream()
                .map(PostData.Reply::from).toList();
    }

    /** @param id 게시글 ID
     * @param userId 댓글 작성자 ID
     * @param content 댓글 본문
     * @return 저장된 댓글 */
    public PostData.Reply reply(Long id, Long userId, String content) {
        Post post = lockPost(id);
        Comment comment = new Comment(users.getReferenceById(userId), content.strip());
        comment.assignPost(post);
        return PostData.Reply.from(comments.saveAndFlush(comment));
    }

    /** @param id 게시글 ID
     * @param commentId 댓글 ID
     * @param userId 댓글 수정 요청자 ID
     * @param content 새 본문
     * @return 수정된 댓글 */
    public PostData.Reply editReply(Long id, Long commentId, Long userId, String content) {
        lockPost(id);
        Comment comment = requireReply(id, commentId, userId);
        comment.updateContent(content.strip());
        return PostData.Reply.from(comment);
    }

    /** @param id 게시글 ID
     * @param commentId 댓글 ID
     * @param userId 삭제 요청자 ID */
    public void deleteReply(Long id, Long commentId, Long userId) {
        lockPost(id);
        requireReply(id, commentId, userId).delete();
    }

    /** @param id 게시글 ID
     * @return 조회 가능한 게시글
     * @throws AuthException 삭제 또는 미존재 상태인 경우 */
    private Post requirePost(Long id) {
        return posts.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
    }

    /** @param id 게시글 ID
     * @return 변경이 완료될 때까지 쓰기 잠금을 얻은 게시글
     * @throws AuthException 삭제 또는 미존재 상태인 경우 */
    private Post lockPost(Long id) {
        return posts.findForUpdate(id)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
    }

    /** @param id 게시글 ID
     * @param commentId 댓글 ID
     * @param userId 작업 요청자 ID
     * @return 해당 게시글에 속한 요청자 자신의 댓글
     * @throws AuthException 댓글이 없거나 작성자가 다른 경우 */
    private Comment requireReply(Long id, Long commentId, Long userId) {
        Comment comment = comments.findByIdAndPostIdAndDeletedAtIsNull(commentId, id)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."));
        requireOwner(comment.getUser(), userId);
        return comment;
    }

    /** @param owner 원래 작성자
     * @param userId 작업 요청자 ID
     * @throws AuthException 작성자가 아닌 경우 */
    private void requireOwner(User owner, Long userId) {
        if (!Objects.equals(owner.getId(), userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "NOT_OWNER", "작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    /** @param post 조회할 게시글
     * @param userId 로그인 사용자 ID 또는 null
     * @return 현재 댓글과 추천 집계를 포함한 게시글 */
    private PostData describe(Post post, Long userId) {
        Long id = post.getId();
        var author = post.getUser();
        return new PostData(id, post.getTitle(), post.getCategory(), post.getContent(),
                author.getProfile().getNickname(), "u_" + author.getId(), post.getCreatedAt(),
                likes.countByPostId(id), comments.countByPostIdAndDeletedAtIsNull(id),
                post.getViewCount(), userId != null && likes.existsByPostIdAndUserId(id, userId));
    }
}
