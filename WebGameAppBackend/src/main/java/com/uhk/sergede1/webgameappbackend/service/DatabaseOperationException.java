package com.uhk.sergede1.webgameappbackend.service;

public class DatabaseOperationException extends Exception {
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseOperationException(String message) {
        super(message);
    }
}