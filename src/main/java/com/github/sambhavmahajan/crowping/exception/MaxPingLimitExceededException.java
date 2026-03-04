package com.github.sambhavmahajan.crowping.exception;

public class MaxPingLimitExceededException extends RuntimeException {
    public MaxPingLimitExceededException() {
        super("Maximum ping limit reached, must delete a ping before creating new");
    }
}