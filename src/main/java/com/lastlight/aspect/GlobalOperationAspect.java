package com.lastlight.aspect;

import com.lastlight.annotation.GlobalInterceptor;
import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.annotation.VerifyObjectParam;
import com.lastlight.common.Constant;
import com.lastlight.exception.CustomException;
import com.lastlight.utils.VerifyUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {
    @Autowired
    private VerifyUtil verifyUtil;

    @Pointcut("@annotation(com.lastlight.annotation.GlobalInterceptor)")
    private void requestInterceptor(){};

    @Around("requestInterceptor()")
    public Object intercept(ProceedingJoinPoint point) throws Throwable {
        //we can and need to transfer it to MethodSignature because we need to get the method params
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        Object[] args = point.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        if(interceptor.checkCode()){
            verifyUtil.verifyCode(parameterAnnotations, args);
        }

        if(interceptor.checkLogin()){
            verifyUtil.verifyLogin(parameterAnnotations, args, Constant.USER_IDENTITY_NORMAL);
        }
        if(interceptor.checkAdministrator()){
            verifyUtil.verifyLogin(parameterAnnotations, args, Constant.USER_IDENTITY_ADMINISTRATOR);
        }

        //if field(checkParams) is set as true
        if(interceptor.checkParams()){
            VerifyUtil.verifyParam(parameterAnnotations, args);
        }

        //execute target method
        return point.proceed();
    }

}
