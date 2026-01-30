package com.minhyung.schedule.log;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LoggerConfig {
    @Bean
    public HikariPoolMXBean hikariPoolMXBean(DataSource dataSource) {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        return hikari.getHikariPoolMXBean();
    }
}
