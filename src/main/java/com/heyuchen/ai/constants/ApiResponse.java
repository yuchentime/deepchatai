package com.heyuchen.ai.constants;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * A generic response wrapper for Spring MVC applications.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * The status of the response (e.g., "success", "error").
     */
    private String status;

    /**
     * The message to be included in the response.
     */
    private String message;

    /**
     * The data payload of the response.
     */
    private T data;

    /**
     * List of errors if any.
     */
    private List<String> errors;

    /**
     * Constructor for success responses.
     *
     * @param data the data payload
     */
    public ApiResponse(T data) {
        this.status = "success";
        this.data = data;
    }

    /**
     * Constructor for error responses.
     *
     * @param message the error message
     * @param errors  list of errors
     */
    public ApiResponse(String message, List<String> errors) {
        this.status = "error";
        this.message = message;
        this.errors = errors;
    }

    public ApiResponse(String status, String message, T data, List<String> errors) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Builder method for success responses.
     *
     * @param data the data payload
     * @return an instance of ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Builder method for error responses.
     *
     * @param message the error message
     * @return an instance of ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .build();
    }
}