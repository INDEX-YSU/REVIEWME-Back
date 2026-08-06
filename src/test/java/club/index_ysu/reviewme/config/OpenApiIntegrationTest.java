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
