package com.lastlight.exception;

public class SpaceNotEnoughException extends RuntimeException{
    public SpaceNotEnoughException(String msg) {
        super(msg);
    }
}
