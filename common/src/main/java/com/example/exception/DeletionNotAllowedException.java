package com.example.exception;

public class DeletionNotAllowedException extends RuntimeException {
    public DeletionNotAllowedException(String msg) {
        super(msg);
    }
}
