package com.heyuchen.ai.exception;

public class GlobalException extends RuntimeException {

    private final String code;
    private final String message;

    public GlobalException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
