package com.heyuchen.ai.exception;

public class BizException extends Exception {

    // Default constructor
    public BizException() {
        super();
    }

    // Constructor with message
    public BizException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with cause
    public BizException(Throwable cause) {
        super(cause);
    }
}
