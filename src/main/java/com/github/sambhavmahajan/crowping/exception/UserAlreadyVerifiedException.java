package com.github.sambhavmahajan.crowping.exception;

public class UserAlreadyVerifiedException extends RuntimeException {
    public UserAlreadyVerifiedException() {
        super("User already verified");
    }
}
