package com.uhk.sergede1.webgameappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WebGameAppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebGameAppBackendApplication.class, args);
    }
}
