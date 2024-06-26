package com.uhk.sergede1.webgameappbackend.rest.free.registration;

import com.uhk.sergede1.webgameappbackend.database_service.exceptions.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewUserRegistrationController {

    @Autowired
    private DatabaseService databaseService;

    @PostMapping(path = "/register")
    public String basicAuthCheck(@RequestBody RegistrationRequest registrationRequest) {
        String username = registrationRequest.username();
        String password = registrationRequest.password();

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);

        try{
            databaseService.saveUser(new User(username, hashedPassword));
        } catch (DatabaseOperationException e){
            if ("User already exists".equals(e.getMessage())) {
                // Handle user already exists scenario
                System.out.println("User with username " + username + " already exists.");
                return "User with username " + username + " already exists.";
            } else if ("Error inserting user into database".equals(e.getMessage())) {
                // Handle database insertion error scenario
                System.out.println("Error inserting user into the database.");
                return "Error inserting user into the database.";
            } else {
                // Handle other exceptions
                System.out.println("An unknown error occurred: " + e.getMessage());
                return "An unknown error occurred: " + e.getMessage();
            }
        }
        return "Success";
    }
}
