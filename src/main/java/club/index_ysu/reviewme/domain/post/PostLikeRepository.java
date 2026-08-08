package club.index_ysu.reviewme.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 게시글 추천의 저장, 중복 확인, 추천 수 집계를 담당하는 Repository입니다.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 사용자가 특정 게시글을 이미 추천했는지 확인합니다.
     *
     * @param postId 게시글 식별자
     * @param userId 사용자 식별자
     * @return 추천이 존재하면 {@code true}
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 추천 취소에 사용할 게시글과 사용자 조합의 추천을 조회합니다.
     *
     * @param postId 게시글 식별자
     * @param userId 사용자 식별자
     * @return 일치하는 추천, 없으면 빈 값
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 특정 게시글에 저장된 추천 수를 집계합니다.
     *
     * @param postId 게시글 식별자
     * @return 추천 수
     */
    long countByPostId(Long postId);
}
