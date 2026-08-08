package club.index_ysu.reviewme.domain.comment;

import club.index_ysu.reviewme.domain.common.BaseTimeEntity;
import club.index_ysu.reviewme.domain.post.Post;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글에 작성된 댓글 또는 대댓글을 나타내는 도메인 엔티티입니다.
 * 자기참조 부모 관계로 댓글 트리를 구성하며, 삭제 시 내용을 제거하지 않고 삭제 시각을 기록해
 * 자식 댓글의 문맥을 유지합니다.
 */
@Getter
@Entity
@Table(name = "COMMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    /** 삭제된 댓글을 조회 응답에서 대신 노출할 고정 문구입니다. */
    public static final String DELETED_CONTENT = "삭제된 댓글입니다";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<Comment> children = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 작성자와 본문으로 최상위 댓글을 생성합니다. 게시글 연결은 {@link Post#addComment(Comment)}가 담당합니다.
     *
     * @param user 댓글 작성자
     * @param content 댓글 본문
     */
    public Comment(User user, String content) {
        this.user = user;
        this.content = content;
    }

    /**
     * 댓글이 속한 게시글을 지정합니다. 자식 댓글을 추가할 때도 부모와 동일한 게시글로 연결됩니다.
     *
     * @param post 댓글이 속한 게시글
     */
    public void assignPost(Post post) {
        this.post = post;
    }

    /**
     * 현재 댓글 아래에 대댓글을 연결하고 부모 및 게시글 연관관계를 함께 설정합니다.
     *
     * @param child 추가할 대댓글
     */
    public void addChild(Comment child) {
        children.add(child);
        child.parent = this;
        child.post = post;
    }

    /**
     * 댓글 본문을 교체합니다. 삭제된 댓글의 수정 가능 여부와 작성자 권한은 서비스 계층에서 검사합니다.
     *
     * @param content 수정할 댓글 본문
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 댓글을 Soft Delete 상태로 전환합니다. 실제 본문은 감사 및 복구 가능성을 위해 보존합니다.
     */
    public void delete() {
        deletedAt = LocalDateTime.now();
    }

    /**
     * 댓글이 삭제되었는지 확인합니다.
     *
     * @return 삭제 시간이 있으면 {@code true}
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * API 응답에 노출할 댓글 내용을 반환하며, 삭제된 댓글은 고정 안내 문구로 치환합니다.
     *
     * @return 삭제 상태가 반영된 표시용 댓글 내용
     */
    public String getDisplayContent() {
        return isDeleted() ? DELETED_CONTENT : content;
    }
}
