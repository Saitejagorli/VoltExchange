package com.saicodes.VoltExchange.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T> (String status, String message, T data, Object errors,LocalDateTime timestamp) {
    public static <T> ApiResponse<T> success(String message,T data) {
        return new ApiResponse<>("OK",message,data,null,LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(String message,Object errors) {
        return new ApiResponse<>("ERROR",message,null,errors,LocalDateTime.now());
    }
}
