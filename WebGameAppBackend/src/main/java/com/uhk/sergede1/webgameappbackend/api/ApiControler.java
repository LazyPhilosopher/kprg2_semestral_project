package com.uhk.sergede1.webgameappbackend.api;

import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiControler {
    @Autowired
    private DatabaseService databaseService;

    @GetMapping(path = "/api/get-user-list")
    public List<User> basicAuthCheck() {

        System.out.println("Got GET request to see list of users");
        List<User> user_list;
        try {
            user_list = databaseService.findAllUsers();
        } catch (Exception e){
            return null;
        }
        return user_list;
    }
}

