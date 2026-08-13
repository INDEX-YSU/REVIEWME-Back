package club.index_ysu.reviewme.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 게시글 애그리거트의 영속성 접근을 제공하는 Spring Data JPA Repository입니다.
 * Soft Delete 된 게시글을 일반 상세 조회에서 제외하는 계약을 제공합니다.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 삭제되지 않은 게시글만 식별자로 조회합니다.
     *
     * @param id 조회할 게시글 식별자
     * @return 삭제되지 않은 게시글, 없거나 삭제되었으면 빈 값
     */
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}