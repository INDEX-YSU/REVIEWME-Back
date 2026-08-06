package club.index_ysu.reviewme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@SpringBootApplication
@RestController
@Tag(name = "System", description = "서버 상태 확인")
public class ReviewmeApplication {

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "서버 응답 확인", description = "애플리케이션이 요청에 응답하는지 확인합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "서버 응답 성공",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Hello Docker World!"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public String home() {
        return "Hello Docker World!";
    }

	public static void main(String[] args) {
		SpringApplication.run(ReviewmeApplication.class, args);
	}
 
}
