package club.index_ysu.reviewme.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";
    public static final String REFRESH_COOKIE = "refreshCookie";

    @Bean
    OpenAPI reviewMeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("REVIEW.ME API")
                        .description("REVIEW.ME 백엔드 REST API 명세")
                        .version("v1.0")
                        .license(new License().name("Private")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 응답으로 받은 Access Token을 입력합니다."))
                        .addSecuritySchemes(REFRESH_COOKIE, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("refreshToken")
                                .description("로그인 시 서버가 발급하는 HttpOnly 쿠키입니다.")));
    }
}
