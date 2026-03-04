package com.github.sambhavmahajan.crowping.exception;

public class EmailOrPasswordEmptyException extends RuntimeException {
    public EmailOrPasswordEmptyException() {
        super("Email or Password can't be empty");
    }
}
