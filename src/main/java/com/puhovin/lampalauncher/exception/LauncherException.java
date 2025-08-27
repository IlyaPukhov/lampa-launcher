package com.puhovin.lampalauncher.exception;

public abstract class LauncherException extends Exception {

    protected LauncherException(String message) {
        super(message);
    }

    protected LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
}