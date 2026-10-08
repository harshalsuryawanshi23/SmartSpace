package com.smartspace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import com.smartspace.dev.MutableClock;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    @Profile("!dev")
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Kolkata"));
    }

    @Bean
    @Profile("dev")
    public Clock devClock() {
        return new MutableClock(ZoneId.of("Asia/Kolkata"));
    }
}
