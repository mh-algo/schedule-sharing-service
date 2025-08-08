package com.minhyung.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ScheduleSharingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleSharingServiceApplication.class, args);
    }

}
