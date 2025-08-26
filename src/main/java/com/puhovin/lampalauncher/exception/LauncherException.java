package com.puhovin.lampalauncher.exception;

public class LauncherException extends Exception {

    public LauncherException(String message) {
        super(message);
    }

    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
}