package com.example.demo.Config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * Single clock for the whole application so token expiry and createdAt timestamps are
     * testable and consistent.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
