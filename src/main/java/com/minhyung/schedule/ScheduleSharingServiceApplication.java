package com.minhyung.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableAsync
@EnableJpaAuditing
@EnableScheduling
@EnableMethodSecurity
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ScheduleSharingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleSharingServiceApplication.class, args);
    }

}
