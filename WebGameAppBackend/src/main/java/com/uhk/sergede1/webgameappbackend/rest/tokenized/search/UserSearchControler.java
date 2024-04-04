package com.uhk.sergede1.webgameappbackend.rest.tokenized.search;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.rest.free.registration.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserSearchControler {
    @Autowired
    private DatabaseService databaseService;

    @GetMapping(path = "/api/get-user-list")
    public List<User> getAllUsers() {

        System.out.println("Got GET request to see list of users");
        List<User> user_list;
        try {
            user_list = databaseService.findAllUsers();
        } catch (Exception e){
            return null;
        }
        return user_list;
    }

    @PostMapping(path = "/api/search-user-username")
    public List<User> searchUserByUsername(@RequestBody UserSearchRequestBody userSearchRequestBody){
        String username = userSearchRequestBody.username();
        System.out.println("searchUserByUsername:"+username);
        List<User> users = new ArrayList<>();
        try {
            users = databaseService.searchUserByUsername(username);
        } catch (DatabaseOperationException e){
            System.out.println("Error occured during search for user '"+username+"'! Exception: "+e);
        }
        return users;
    }
}

