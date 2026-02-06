package com.example.ediprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EdiProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdiProcessorApplication.class, args);
    }

}
