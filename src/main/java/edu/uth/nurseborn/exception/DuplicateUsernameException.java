package edu.uth.nurseborn.exception;

public class DuplicateUsernameException extends  RuntimeException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
