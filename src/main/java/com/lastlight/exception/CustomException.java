package com.lastlight.exception;

public class CustomException extends RuntimeException{
    private static final CustomException illegal = new CustomException("非法操作");
    public CustomException(String message) {
        super(message);
    }
    public static CustomException illegal(){
        return illegal;
    }
}
