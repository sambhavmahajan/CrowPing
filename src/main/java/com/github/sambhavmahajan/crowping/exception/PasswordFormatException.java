package com.github.sambhavmahajan.crowping.exception;

public class PasswordFormatException extends RuntimeException {
    public PasswordFormatException() {
        super("Password should contain at least one digit\nPassword should contain at least one lowercase letter\nPassword should contain at least one lowercase letter");
    }
}
