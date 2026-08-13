package club.index_ysu.reviewme.domain.post;

import club.index_ysu.reviewme.domain.common.BaseTimeEntity;
import club.index_ysu.reviewme.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 한 사용자가 한 게시글에 남긴 추천을 나타내는 연결 엔티티입니다.
 * 게시글과 사용자 조합의 유일 제약으로 동시 요청에서도 중복 추천이 저장되지 않도록 보장합니다.
 */
@Getter
@Entity
@Table(name = "POST_LIKE", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_like_post_user", columnNames = {"post_id", "user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 추천한 사용자를 지정해 새 추천을 생성합니다. 게시글은 {@link Post#addLike(PostLike)}로 연결합니다.
     *
     * @param user 추천한 사용자
     */
    public PostLike(User user) {
        this.user = user;
    }

    /**
     * 추천 대상 게시글을 지정합니다.
     *
     * @param post 추천 대상 게시글
     */
    public void assignPost(Post post) {
        this.post = post;
    }
}
