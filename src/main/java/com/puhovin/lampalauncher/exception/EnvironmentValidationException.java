package com.puhovin.lampalauncher.exception;

public class EnvironmentValidationException extends LauncherException {

    public EnvironmentValidationException(String message) {
        super(message);
    }

    public EnvironmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
