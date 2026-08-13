package club.index_ysu.reviewme.domain.post;

import club.index_ysu.reviewme.domain.comment.Comment;
import club.index_ysu.reviewme.domain.common.BaseTimeEntity;
import club.index_ysu.reviewme.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자가 작성한 게시글과 게시글의 노출 상태 및 통계 정보를 나타내는 도메인 엔티티입니다.
 * 작성자 {@link User}, 댓글 {@link Comment}, 추천 {@link PostLike}과 연관되며 게시글 API의
 * 생성, 조회, 수정, 삭제 흐름에서 영속화되는 중심 애그리거트입니다.
 */
@Getter
@Entity
@Table(name = "POST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<PostLike> likes = new ArrayList<>();

    /**
     * 게시글 작성 요청의 핵심 필드로 새 게시글을 생성합니다.
     * 작성자는 연관관계 편의 메서드인 {@link User#addPost(Post)}를 통해 연결합니다.
     *
     * @param title 게시글 제목
     * @param content 게시글 본문
     * @param category 게시글 분류를 나타내는 확장 가능한 문자열 코드
     */
    @Builder
    private Post(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    /**
     * 게시글의 작성자를 지정합니다. 양방향 컬렉션의 일관성은 호출 측에서 관리합니다.
     *
     * @param user 게시글을 작성한 사용자
     */
    public void assignUser(User user) {
        this.user = user;
    }

    /**
     * 게시글 내용을 새 값으로 교체합니다. JPA 변경 감지를 통해 수정 시간이 함께 갱신됩니다.
     *
     * @param title 수정할 제목
     * @param content 수정할 본문
     * @param category 수정할 카테고리 코드
     */
    public void update(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    /**
     * 게시글 조회수를 한 번 증가시킵니다. 중복 조회 방지는 상위 애플리케이션 서비스가 담당합니다.
     */
    public void increaseViewCount() {
        viewCount++;
    }

    /**
     * 게시글을 물리적으로 제거하지 않고 현재 시각을 기록해 삭제 상태로 전환합니다.
     */
    public void delete() {
        deletedAt = LocalDateTime.now();
    }

    /**
     * 게시글이 Soft Delete 처리되었는지 확인합니다.
     *
     * @return 삭제 시간이 기록되었으면 {@code true}
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 댓글을 게시글에 연결하고 댓글 측 연관관계도 함께 설정합니다.
     *
     * @param comment 연결할 댓글
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.assignPost(this);
    }

    /**
     * 추천을 게시글에 연결하고 추천 측 연관관계도 함께 설정합니다.
     *
     * @param postLike 연결할 추천
     */
    public void addLike(PostLike postLike) {
        likes.add(postLike);
        postLike.assignPost(this);
    }
}
