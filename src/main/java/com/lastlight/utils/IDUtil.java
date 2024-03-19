package com.lastlight.utils;

import java.util.Random;
import java.util.UUID;

public class IDUtil {
    public static final String charset = "0123456789abcdefg";
    public static String getUUID(){
        UUID uuid = UUID.randomUUID();
        //deleted the sign '-' in uuid
        return uuid.toString().replace("-","");
    }

    public static String getRandomId(){
        long timeStamp = System.currentTimeMillis();
        //前3位重复度可能比较高，因而直接舍去
        String res = new String(String.valueOf(timeStamp).substring(3));
        Random random = new Random();
        for(int i=0; i<6; ++i){
            res += charset.charAt(random.nextInt(0,16));
        }
        return res;
    }

    public static String getRandomId(int length){
        var res = "";
        Random random = new Random();
        for(int i=0; i<length; ++i){
            res += charset.charAt(random.nextInt(0,16));
        }
        return res;
    }
}
