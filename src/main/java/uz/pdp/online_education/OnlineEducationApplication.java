package uz.pdp.online_education;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class OnlineEducationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineEducationApplication.class, args);

        System.out.println("Hello, Online Education Application is running!");
    }

}
