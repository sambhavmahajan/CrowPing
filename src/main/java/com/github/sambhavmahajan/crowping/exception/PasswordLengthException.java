package com.github.sambhavmahajan.crowping.exception;

public class PasswordLengthException extends RuntimeException {
    public PasswordLengthException() {
        super("Password length cannot be less than 6 characters and at  most 32 characters");
    }
}
