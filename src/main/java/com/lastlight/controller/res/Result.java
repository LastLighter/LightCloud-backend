package com.lastlight.controller.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private ResStatus status;
    private T data;
    public static <T> Result success(T data){
        return new Result(ResStatus.OK,data);
    }

    public static <T> Result error(ResStatus status){
        return new Result(status,null);
    }
}
