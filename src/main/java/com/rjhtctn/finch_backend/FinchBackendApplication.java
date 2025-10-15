package com.rjhtctn.finch_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinchBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(FinchBackendApplication.class, args);
    }
}