package com.github.sambhavmahajan.crowping.exception;

public class ConfirmTokenExpiredException extends RuntimeException {
    public ConfirmTokenExpiredException() {
        super("Token has expired");
    }
}
