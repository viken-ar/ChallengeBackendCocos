package ar.com.cocos.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChallengeBackendCocosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallengeBackendCocosApplication.class, args);
    }

}
