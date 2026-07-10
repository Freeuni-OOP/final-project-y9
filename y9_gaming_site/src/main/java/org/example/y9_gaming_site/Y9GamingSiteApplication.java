package org.example.y9_gaming_site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Y9GamingSiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(Y9GamingSiteApplication.class, args);
    }

}