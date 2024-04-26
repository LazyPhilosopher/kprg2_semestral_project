package com.uhk.sergede1.webgameappbackend.database_service.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}