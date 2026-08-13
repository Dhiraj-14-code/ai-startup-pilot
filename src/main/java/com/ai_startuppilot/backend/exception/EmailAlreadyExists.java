package com.ai_startuppilot.backend.exception;

public class EmailAlreadyExists extends RuntimeException{
    public EmailAlreadyExists(String message) {
        super(message);
    }
}
