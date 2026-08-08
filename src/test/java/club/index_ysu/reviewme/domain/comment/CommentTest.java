package club.index_ysu.reviewme.domain.comment;

import club.index_ysu.reviewme.domain.post.Post;
import club.index_ysu.reviewme.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 댓글의 대댓글 연관관계, 수정 및 Soft Delete 표시 정책을 검증합니다.
 */
class CommentTest {

    /** 대댓글을 추가하면 부모와 게시글 연관관계를 상속해야 합니다. */
    @Test
    void addChildConnectsParentAndPost() {
        User user = createUser();
        Post post = Post.builder().title("제목").content("본문").category("PROJECT").build();
        Comment parent = new Comment(user, "부모 댓글");
        Comment child = new Comment(user, "대댓글");
        post.addComment(parent);

        parent.addChild(child);

        assertThat(child.getParent()).isSameAs(parent);
        assertThat(child.getPost()).isSameAs(post);
        assertThat(parent.getChildren()).containsExactly(child);
    }

    /** 삭제 전에는 실제 본문을, 삭제 후에는 명세의 삭제 안내 문구를 반환해야 합니다. */
    @Test
    void displayContentMasksDeletedComment() {
        Comment comment = new Comment(createUser(), "원본 댓글");

        assertThat(comment.getDisplayContent()).isEqualTo("원본 댓글");

        comment.delete();

        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDisplayContent()).isEqualTo(Comment.DELETED_CONTENT);
        assertThat(comment.getContent()).isEqualTo("원본 댓글");
    }

    /** 댓글 수정 시 저장된 본문이 새 값으로 교체되어야 합니다. */
    @Test
    void updateContentReplacesCommentBody() {
        Comment comment = new Comment(createUser(), "원본 댓글");

        comment.updateContent("수정 댓글");

        assertThat(comment.getContent()).isEqualTo("수정 댓글");
    }

    /** 테스트용 활성 사용자를 생성합니다. */
    private User createUser() {
        return User.builder().email("author@example.com").password("encoded").build();
    }
}
