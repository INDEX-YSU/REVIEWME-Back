package club.index_ysu.reviewme;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 기존 이메일 인증과 MVP 게시판, 프로필, 채팅 기능의 HTTP 계약을 확인합니다. */
@SpringBootTest
@AutoConfigureMockMvc
class MvpApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void publicReadingAndAuthenticatedContributionsRespectOwnershipAndDeletedState() throws Exception {
        var author = account("post-author");
        var reviewer = account("post-reviewer");
        String draft = """
                {"title":"Review request","content":"Review this code","category":"백엔드"}
                """;
        mvc.perform(get("/api/v1/posts")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content(draft))
                .andExpect(status().isUnauthorized());
        request(post("/api/v1/posts"), author, "{\"title\":\" \",\"content\":\"body\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        int id = value(request(post("/api/v1/posts"), author, draft).andExpect(status().isCreated()), "$.data.postId");
        String path = "/api/v1/posts/" + id;
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data.category").value("백엔드"))
                .andExpect(jsonPath("$.data.author").value("post-author"));
        mvc.perform(post(path + "/views")).andExpect(status().isOk()).andExpect(jsonPath("$.data").value(1));
        request(put(path), reviewer, draft).andExpect(status().isForbidden());
        request(delete(path), reviewer, null).andExpect(status().isForbidden());
        request(put(path), author, draft.replace("Review request", "Updated"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("Updated"));
        mvc.perform(put(path + "/like")).andExpect(status().isUnauthorized());
        request(put(path + "/like"), reviewer, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likedByMe").value(true));
        request(put(path + "/like"), reviewer, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likes").value(1));
        request(delete(path + "/like"), reviewer, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likes").value(0));

        String comments = path + "/comments";
        request(post(comments), reviewer, "{\"content\":\" \"}").andExpect(status().isBadRequest());
        int replyId = value(request(post(comments), reviewer, "{\"content\":\"First reply\"}")
                .andExpect(status().isCreated()), "$.data.id");
        mvc.perform(get(comments)).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        request(put(comments + "/" + replyId), author, "{\"content\":\"Not allowed\"}")
                .andExpect(status().isForbidden());
        request(delete(comments + "/" + replyId), author, null).andExpect(status().isForbidden());
        request(put(comments + "/" + replyId), reviewer, "{\"content\":\"Edited reply\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("Edited reply"));
        request(delete(comments + "/" + replyId), reviewer, null).andExpect(status().isNoContent());
        mvc.perform(get(comments)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        request(delete(path), author, null).andExpect(status().isNoContent());
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(get(comments)).andExpect(status().isNotFound());
    }

    @Test
    void profileRequiresAuthenticationAndPersistsValidatedProfileFields() throws Exception {
        var token = account("profile-user");
        String path = "/api/v1/profile";
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        request(put(path), token, "{\"nickname\":\"\"}").andExpect(status().isBadRequest());
        request(put(path), token, """
                {"nickname":"Reviewer","bio":"I review code","affiliation":"INDEX","techStack":["Java","React","Java"]}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.data.techStack.length()").value(2));
        request(get(path), token, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Reviewer"))
                .andExpect(jsonPath("$.data.affiliation").value("INDEX"));
    }

    @Test
    void privateChatRequiresAnInviteAndOnlyMessageAuthorMayDelete() throws Exception {
        var host = account("chat-host");
        var invited = account("chat-invited");
        var outsider = account("chat-outsider");
        String roomList = "/api/v1/chat/rooms";
        mvc.perform(get(roomList)).andExpect(status().isOk());
        request(post(roomList), host, "{}").andExpect(status().isBadRequest());
        int id = value(request(post(roomList), host, """
                {"title":"Private review", "description":"Discussion", "category":"자유", "isPrivate":true, "allowGuest":true}
                """).andExpect(status().isCreated()).andExpect(jsonPath("$.data.isPrivate").value(true))
                .andExpect(jsonPath("$.data.allowGuest").value(false)), "$.data.id");
        String room = roomList + "/" + id;
        String messages = room + "/messages";
        mvc.perform(get(room)).andExpect(status().isForbidden());
        request(get(roomList), invited, null).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        request(get(messages), invited, null).andExpect(status().isForbidden());
        request(post(messages), invited, "{\"content\":\"Cannot send\"}").andExpect(status().isForbidden());
        request(post(room + "/invites"), host, "{\"minutes\":0}").andExpect(status().isBadRequest());
        String code = value(request(post(room + "/invites"), host, "{\"minutes\":60}")
                .andExpect(status().isCreated()), "$.data.token");
        String accept = "/api/v1/chat/invites/" + code + "/accept";
        mvc.perform(post(accept)).andExpect(status().isUnauthorized());
        request(post(accept), invited, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participants").value(2));
        request(post(accept), invited, null).andExpect(status().isOk());
        request(post(accept), outsider, null).andExpect(status().isGone());
        request(post("/api/v1/chat/invites/missing/accept"), outsider, null).andExpect(status().isNotFound());
        request(post(room + "/invites"), invited, "{\"minutes\":60}").andExpect(status().isForbidden());
        request(post(messages), invited, "{\"content\":\"  \"}").andExpect(status().isBadRequest());
        int message = value(request(post(messages), invited, "{\"content\":\"Thanks!\"}")
                .andExpect(status().isCreated()), "$.data.id");
        request(get(messages), host, null).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].content").value("Thanks!"));
        request(get(messages + "?before=" + message), host, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        request(delete(messages + "/" + message), host, null).andExpect(status().isForbidden());
        request(delete(messages + "/" + (message + 1)), invited, null).andExpect(status().isNotFound());
        request(delete(messages + "/" + message), invited, null).andExpect(status().isNoContent());
        request(get(messages), host, null).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].isDeleted").value(true))
                .andExpect(jsonPath("$.data[0].content").value(""));
    }

    @Test
    void publicChatGuestReadingIsConfigurableAndSendingRequiresLogin() throws Exception {
        var host = account("public-host");
        String rooms = "/api/v1/chat/rooms";
        int publicId = value(request(post(rooms), host, """
                {"title":"Public review","description":"","category":"자유","allowGuest":true}
                """).andExpect(status().isCreated()), "$.data.id");
        int membersId = value(request(post(rooms), host, """
                {"title":"Members only","description":"","category":"자유","allowGuest":false}
                """).andExpect(status().isCreated()), "$.data.id");
        mvc.perform(get(rooms + "/" + publicId)).andExpect(status().isOk());
        mvc.perform(get(rooms + "/" + membersId)).andExpect(status().isUnauthorized());
        mvc.perform(post(rooms + "/" + publicId + "/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Guest\"}")).andExpect(status().isUnauthorized());
    }

    /** @param name 고유한 테스트 계정 이름
     * @return 회원가입 후 로그인으로 발행한 토큰
     * @throws Exception HTTP 테스트 요청 실행 실패 */
    private String account(String name) throws Exception {
        String email = name + "@mvp.example";
        String signup = """
                {"email":"%s","password":"P@ssw0rd123","nickname":"%s"}
                """.formatted(email, name);
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signup))
                .andExpect(status().isCreated());
        var login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(signup)).andExpect(status().isOk());
        return value(login, "$.data.accessToken");
    }

    /** @param request HTTP 요청
     * @param token 테스트에서 발행한 토큰
     * @param body 선택적 JSON 본문
     * @return 결과 검증 객체
     * @throws Exception HTTP 테스트 요청 실행 실패 */
    private ResultActions request(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request);
    }

    /** @param result HTTP 테스트 결과
     * @param path 추출할 JSON 경로
     * @param <T> 필드 타입
     * @return 지정한 응답 필드 값
     * @throws Exception 응답 읽기 실패 */
    private <T> T value(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }
}
