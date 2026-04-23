package com.hft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HftApplication {
    public static void main(String[] args) {
        SpringApplication.run(HftApplication.class, args);
        System.out.println("\n=== HFT Phase 2 Matching Engine Started on Port 8080 ===\n");
    }
}
