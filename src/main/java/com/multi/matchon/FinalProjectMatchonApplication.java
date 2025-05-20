package com.multi.matchon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "loginUserAuditorAware")
@SpringBootApplication
public class FinalProjectMatchonApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalProjectMatchonApplication.class, args);
    }

}
