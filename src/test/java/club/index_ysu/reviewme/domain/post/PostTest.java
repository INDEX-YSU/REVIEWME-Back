package club.index_ysu.reviewme.domain.post;

import club.index_ysu.reviewme.domain.comment.Comment;
import club.index_ysu.reviewme.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 게시글 엔티티의 수정, 조회수, Soft Delete 및 연관관계 동작을 검증합니다.
 */
class PostTest {

    /** 게시글 수정 요청의 모든 변경 가능 필드가 함께 반영되어야 합니다. */
    @Test
    void updateReplacesEditableFields() {
        Post post = createPost();

        post.update("수정 제목", "수정 본문", "STUDY");

        assertThat(post.getTitle()).isEqualTo("수정 제목");
        assertThat(post.getContent()).isEqualTo("수정 본문");
        assertThat(post.getCategory()).isEqualTo("STUDY");
    }

    /** 조회수 증가와 Soft Delete 상태 전환은 서로 독립적으로 반영되어야 합니다. */
    @Test
    void increaseViewCountAndDeleteChangePostState() {
        Post post = createPost();

        post.increaseViewCount();
        post.delete();

        assertThat(post.getViewCount()).isOne();
        assertThat(post.isDeleted()).isTrue();
        assertThat(post.getDeletedAt()).isNotNull();
    }

    /** 댓글과 추천을 추가하면 양쪽 객체의 연관관계가 함께 설정되어야 합니다. */
    @Test
    void addRelationsSynchronizesBothSides() {
        User user = User.builder().email("author@example.com").password("encoded").build();
        Post post = createPost();
        Comment comment = new Comment(user, "댓글");
        PostLike like = new PostLike(user);

        post.addComment(comment);
        post.addLike(like);

        assertThat(comment.getPost()).isSameAs(post);
        assertThat(like.getPost()).isSameAs(post);
        assertThat(post.getComments()).containsExactly(comment);
        assertThat(post.getLikes()).containsExactly(like);
    }

    /** 테스트에서 사용하는 유효한 기본 게시글을 생성합니다. */
    private Post createPost() {
        return Post.builder().title("제목").content("본문").category("PROJECT").build();
    }
}
