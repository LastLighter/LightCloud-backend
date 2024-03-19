package com.lastlight.utils;

import com.lastlight.exception.CustomException;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;

public class StringUtil {
    public static boolean isEmpty(String value){
        if(value == null || value.length() == 0){
            return true;
        }
        return false;
    }

    public static String encodeWithMd5(String source){
        try {
            return DigestUtils.md5DigestAsHex(source.getBytes("UTF-8"));
        }catch (UnsupportedEncodingException e){
            throw new CustomException("不支持的编码类型");
        }
    }

    public static boolean contains(String[] list, String value){
        for(String item: list){
            if(item == value){
                return true;
            }
        }
        return false;
    }

    public static boolean isEnabledPath(String path){
        if(path.contains("..")){
            return false;
        }
        return true;
    }
}
