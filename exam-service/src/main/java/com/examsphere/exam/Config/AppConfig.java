package com.examsphere.exam.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    Clock clock(@Value("${app.zone-offset-minutes:330}") long offsetMinutes) {
        return Clock.offset(Clock.systemUTC(), Duration.ofMinutes(offsetMinutes));
    }

}
