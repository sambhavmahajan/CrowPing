package com.github.sambhavmahajan.crowping.exception;


public class NoSuchPingUrlExistsException extends RuntimeException {
    public NoSuchPingUrlExistsException() {
        super("Invalid ping url provided!");
    }
}
