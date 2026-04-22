package com.dropclip.dropclip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

@SpringBootApplication(exclude = {FlywayAutoConfiguration.class})
public class DropclipApplication {
    public static void main(String[] args) {
        SpringApplication.run(DropclipApplication.class, args);
    }
}