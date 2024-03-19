package com.lastlight.exception;

import com.lastlight.controller.res.ResStatus;
import com.lastlight.controller.res.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 全局异常处理方法
     *
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandle(Exception e) {
        log.error(e.getClass() + ":" + e.getMessage());
        return Result.error(new ResStatus("500", "系统异常"));
    }
    @ExceptionHandler(CustomException.class)
    public Result<String> customExceptionHandle(Exception e) {
        log.error(e.getClass() + ":" + e.getMessage());
        return Result.error(new ResStatus("500", e.getMessage()));
    }

    @ExceptionHandler(TokenException.class)
    public Result<String> tokenExceptionHandle(Exception e) {
        log.error(e.getClass() + ":" + e.getMessage());
        if(e.getMessage() == null)
            return Result.error(ResStatus.TOKEN_EXPIRED_ERROR);
        else{
            return Result.error(new ResStatus("401", e.getMessage()));
        }
    }

    @ExceptionHandler(SpaceNotEnoughException.class)
    public Result<String> spaceExceptionHandle(Exception e) {
        log.error(e.getClass() + ":" + e.getMessage());
        return Result.error(ResStatus.SPACE_NOT_ENOUGH_ERROR);
    }
}
