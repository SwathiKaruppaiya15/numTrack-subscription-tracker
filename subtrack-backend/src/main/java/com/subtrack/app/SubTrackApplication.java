package com.subtrack.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubTrackApplication.class, args);
    }
}
