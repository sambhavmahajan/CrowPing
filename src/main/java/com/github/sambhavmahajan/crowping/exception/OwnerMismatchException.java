package com.github.sambhavmahajan.crowping.exception;

public class OwnerMismatchException extends RuntimeException {
    public OwnerMismatchException() {
        super("Owner mismatch");
    }
}
