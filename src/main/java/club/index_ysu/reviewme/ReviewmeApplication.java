package club.index_ysu.reviewme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ReviewmeApplication {
 
    @RequestMapping("/")
    public String home() {
        return "Hello Docker World!";
    }

	public static void main(String[] args) {
		SpringApplication.run(ReviewmeApplication.class, args);
	}
 
}
