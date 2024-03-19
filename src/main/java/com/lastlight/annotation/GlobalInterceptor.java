package com.lastlight.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
    /**
     * check method params
     * @return
     */
    boolean checkParams() default false;
    boolean checkLogin() default false;
    boolean checkCode() default false;
    boolean checkAdministrator() default false;
}
