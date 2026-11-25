package club.index_ysu.reviewme.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 트리 조회와 댓글 영속화를 제공하는 Spring Data JPA Repository입니다.
 * 최상위 댓글만 페이지네이션하고 자식 댓글은 연관관계를 통해 구성하는 계약을 제공합니다.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 최상위 댓글을 페이지 단위로 조회합니다.
     *
     * @param postId 게시글 식별자
     * @param pageable 페이지 및 정렬 조건
     * @return 최상위 댓글 페이지
     */
    Page<Comment> findByPostIdAndParentIsNull(Long postId, Pageable pageable);

    /**
     * 특정 게시글에 속한 전체 댓글 수를 집계합니다. Soft Delete 댓글도 트리 문맥 유지를 위해 포함합니다.
     *
     * @param postId 게시글 식별자
     * @return 게시글의 전체 댓글 수
     */
    long countByPostId(Long postId);

    /** @param postId 게시글 ID
     * @return 삭제되지 않은 댓글을 작성 순서대로 반환합니다. */
    @EntityGraph(attributePaths = {"user", "user.profile"})
    List<Comment> findAllByPostIdAndDeletedAtIsNullOrderByIdAsc(Long postId);

    /** @param id 댓글 ID
     * @param postId 댓글이 속한 게시글 ID
     * @return 삭제되지 않은 해당 댓글 */
    Optional<Comment> findByIdAndPostIdAndDeletedAtIsNull(Long id, Long postId);

    /** @param postId 게시글 ID
     * @return 사용자에게 표시할 댓글 수 */
    long countByPostIdAndDeletedAtIsNull(Long postId);
}
