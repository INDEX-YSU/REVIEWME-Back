package club.index_ysu.reviewme.domain.user;

import club.index_ysu.reviewme.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "USER_PROFILE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 150)
    private String affiliation;

    @Column(name = "tech_stack", columnDefinition = "TEXT")
    private String techStack;

    /** 공개 프로필의 편집 가능 필드를 JPA 변경 감지로 저장합니다.
     * @param nickname 표시 이름
     * @param bio 자기소개
     * @param affiliation 소속
     * @param techStack 줄바꿈으로 구분한 기술 이름 */
    public void update(String nickname, String bio, String affiliation, String techStack) {
        this.nickname = nickname;
        this.bio = bio;
        this.affiliation = affiliation;
        this.techStack = techStack;
    }

    @Builder
    private UserProfile(String nickname, String avatarUrl, String bio) {
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
