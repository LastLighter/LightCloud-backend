package com.lastlight.annotation;

import com.lastlight.common.RegularConstant;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface VerifyBaseParam {
    //the min value, maybe length, maybeNumber, depended on type
    long min() default Integer.MIN_VALUE;
    //the max value, maybe length, maybeNumber, depended on type
    long max() default Integer.MAX_VALUE;
    boolean required() default false;
    RegularConstant regEx() default RegularConstant.REG_NULL;
}
