package com.amiqt.fintrackpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FinTrackWorkersApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinTrackWorkersApplication.class, args);
    }
}
