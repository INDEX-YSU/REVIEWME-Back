package club.index_ysu.reviewme.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class   OpenApiIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openApiExposesMvpResponsesRequestSchemasAndAccessRules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/posts'].get.responses['200'].content").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts'].post.requestBody.content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts'].post.responses['201'].content").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}'].get.responses['404'].content").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}'].put.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/views'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/like'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/like'].delete.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/comments'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/comments'].post.responses['400'].content").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/comments/{commentId}'].put.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/posts/{id}/comments/{commentId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/profile'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/profile'].put.requestBody.content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms/{id}'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms/{id}/messages'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms/{id}/messages'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms/{id}/messages/{messageId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/rooms/{id}/invites'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat/invites/{token}/accept'].post.responses['410'].content").exists());
    }

    @Test
    void openApiDocumentExposesAuthenticationEndpointsAndSecuritySchemes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.info.title").value("REVIEW.ME API"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/signup'].post.responses['201'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/signup'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/signup'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['200'].headers['Set-Cookie']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.security[0].refreshCookie").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/logout'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/logout'].post.responses['200'].headers['Set-Cookie']").exists())
                .andExpect(jsonPath("$.paths['/'].get.responses['200'].content['text/plain'].schema").exists())
                .andExpect(jsonPath("$.paths['/'].post").doesNotExist())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.refreshCookie.in").value("cookie"))
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists())
                .andExpect(jsonPath("$.components.schemas.LoginSuccessResponse").exists());
    }

    @Test
    void swaggerUiIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
