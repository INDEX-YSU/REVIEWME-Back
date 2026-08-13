package club.index_ysu.reviewme.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
