package com.example.demospringai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DemoSpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoSpringAiApplication.class, args);
    }
}
