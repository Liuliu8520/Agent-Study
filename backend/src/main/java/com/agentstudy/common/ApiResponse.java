package com.agentstudy.common;

public record ApiResponse<T>(int code, String message, String errorType, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", null, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return fail(message, "SYSTEM_ERROR");
    }

    public static <T> ApiResponse<T> fail(String message, String errorType) {
        return new ApiResponse<>(-1, message, errorType, null);
    }
}
