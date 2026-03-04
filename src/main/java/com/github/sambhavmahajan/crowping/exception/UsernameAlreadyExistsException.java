package com.github.sambhavmahajan.crowping.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("user with email " + username + " already exists");
    }
}
