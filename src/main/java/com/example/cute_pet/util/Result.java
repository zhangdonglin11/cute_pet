package com.example.cute_pet.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result(T data) {
        this.code = 1;
        this.message = "success";
        this.data = data;
    }

    public Result(T data, boolean success, String message) {
        if (success) {
            this.code = 1;
            this.message = message;
        } else {
            this.code = 0;
            this.message = message;
        }
        this.data = data;
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(0, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message);
    }
}


