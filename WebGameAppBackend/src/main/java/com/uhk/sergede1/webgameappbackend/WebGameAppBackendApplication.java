package com.uhk.sergede1.webgameappbackend;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WebGameAppBackendApplication implements CommandLineRunner {
    @Autowired
    private DatabaseService databaseService;
    public static void main(String[] args) {
        SpringApplication.run(WebGameAppBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Perform your initial database check here
        // For example, check if a specific table exists or insert some initial data
        databaseService.performInitialCheck();
    }
}
