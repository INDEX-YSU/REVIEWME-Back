package club.index_ysu.reviewme.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
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

    /** @return 삭제되지 않은 게시글과 작성자 프로필의 최신순 목록 */
    @EntityGraph(attributePaths = {"user", "user.profile"})
    List<Post> findAllByDeletedAtIsNullOrderByIdDesc();

    /** @param id 게시글 ID
     * @return 다른 쓰기 작업과 충돌하지 않도록 잠근 활성 게시글 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id and p.deletedAt is null")
    Optional<Post> findForUpdate(@Param("id") Long id);
}
