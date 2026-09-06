package com.arenapointhub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.arenapointhub.api")
public class ArenaPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArenaPointApplication.class, args);
    }
}