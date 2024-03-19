package com.lastlight.utils;

import com.lastlight.annotation.*;
import com.lastlight.common.RegularConstant;
import com.lastlight.config.AppConfig;
import com.lastlight.entity.dto.UserDto;
import com.lastlight.entity.vo.CodeVo;
import com.lastlight.exception.CustomException;
import com.lastlight.security.UserSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class VerifyUtil {
    @Autowired
    private UserSecurity userSecurity;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private CheckCodeUtil checkCodeUtil;

    public static boolean regVerify(String reg, String value){
        if(StringUtil.isEmpty(reg)){
            return true;
        }else if(StringUtil.isEmpty(value)){
            return false;
        }
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean regVerify(RegularConstant reg,String value){
        return regVerify(reg.getReg(), value);
    }

    public static boolean checkParam(Object arg, VerifyBaseParam paramAnnotation){
        //check value if it's null
        if (arg == null) {
            return !paramAnnotation.required();
        }

        if(arg instanceof Number){
            long value = ((Number) arg).longValue();
            //range check
            if(value >= paramAnnotation.min() && value <= paramAnnotation.max()){
                return true;
            }
        }else if(arg instanceof String){
            String value = (String) arg;
            //range check
            if(value.length() < paramAnnotation.min() && value.length() > paramAnnotation.max()){
                return false;
            }
            //regEx check
            if(paramAnnotation.regEx() == RegularConstant.REG_NULL){
                return true;
            }else {
                return regVerify(paramAnnotation.regEx(), value);
            }
        }
        return false;
    }

    //check a list of arguments.
    public static void verifyParam(Annotation[][] annotations, Object[] args){
        //the params is sorted as the order there are defined.So we don't need to worry about it.
        for(int i = 0; i < args.length; i++){
            verifyParam(annotations[i], args[i]);
        }
    }

    //check single argument
    public static void verifyParam(Annotation[] annotations, Object arg){
        //a param can contain many annotations in it.
        for(int j = 0; j < annotations.length; j++) {
            //if the param is base type
            if (annotations[j] instanceof VerifyBaseParam) {
                //check the param
                boolean res = checkParam(arg, (VerifyBaseParam) annotations[j]);
                //reject execution if the check result is false
                if (!res) {
                    throw new CustomException("result of checking param is bad.");
                }
            }

            //if the param type is object
            if (annotations[j] instanceof VerifyObjectParam) {
                Field[] fields = arg.getClass().getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] childAnnotations = field.getAnnotations();
                    try {
                        field.setAccessible(true);
                        verifyParam(childAnnotations, field.get(arg));
                    }catch (IllegalAccessException e){
                        log.info(e.getMessage());
                    }
                }
            }
        }
    }

    public void verifyLogin(Annotation[][] annotations, Object[] args, String userIdentity){
        //the params is sorted as the order there are defined.So we don't need to worry about it.
        for(int i = 0; i < args.length; i++){
            verifyLogin(annotations[i], args[i], userIdentity);
        }
    }

    public void verifyLogin(Annotation[] annotations, Object arg, String userIdentity){
        for(int j = 0; j < annotations.length; j++) {
            if(annotations[j] instanceof Token){
                checkLogin(arg, (Token) annotations[j], userIdentity);
            }

            //if the param type is object
            if (annotations[j] instanceof TokenObject) {
                Field[] fields = arg.getClass().getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] childAnnotations = field.getAnnotations();
                    try {
                        field.setAccessible(true);
                        verifyLogin(childAnnotations, field.get(arg), userIdentity);
                    }catch (IllegalAccessException e){
                        log.info(e.getMessage());
                    }
                }
            }
        }
    }

    public void checkLogin(Object token, Token tokenAnnotation, String userIdentity){
        userSecurity.checkToken(String.valueOf(token), null, appConfig.getTokenExpiredTime(), userIdentity);
    }

    public void verifyCode(Annotation[][] annotations, Object[] args){
        //the params is sorted as the order there are defined.So we don't need to worry about it.
        CodeVo codeVo = new CodeVo();
        getCode(annotations, args, codeVo);
        checkCode(codeVo.getCode(), codeVo.getCodeKey());
    }
    public void getCode(Annotation[][] annotations, Object[] args,CodeVo codeVo){
        for(int i = 0; i < args.length; i++){
            getCode(annotations[i], args[i], codeVo);
        }
    }


    public void getCode(Annotation[] annotations, Object arg, CodeVo codeVo){
        for(int j = 0; j < annotations.length; j++) {
            if(annotations[j] instanceof Code){
                codeVo.setCode(String.valueOf(arg));
            }

            if(annotations[j] instanceof CodeKey){
                codeVo.setCodeKey(String.valueOf(arg));
            }

            //if the param type is object
            if (annotations[j] instanceof CodeObject) {
                Field[] fields = arg.getClass().getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] childAnnotations = field.getAnnotations();
                    try {
                        field.setAccessible(true);
                        getCode(childAnnotations, field.get(arg),codeVo);
                    }catch (IllegalAccessException e){
                        log.info(e.getMessage());
                    }
                }
            }
        }
    }

    public void checkCode(String code, String codeKey){
        //check code
        if(!checkCodeUtil.checkCode(code, codeKey))
            throw new CustomException("校验验证码不匹配");
    }
}
