package com.example.demo.util.errors;

public class IdInvalidException extends Exception {
    // Constructor that accepts a message
    public IdInvalidException(String message) {
        super(message);
    }
}