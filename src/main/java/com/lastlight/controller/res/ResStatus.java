package com.lastlight.controller.res;

import lombok.Data;

@Data
public class ResStatus {
    public static ResStatus OK = new ResStatus("200","正常");
    public static ResStatus NOT_FOUND = new ResStatus("404","未找到访问的资源");
    public static ResStatus ERROR = new ResStatus("500","服务器程序错误");
    public static ResStatus TOKEN_EXPIRED_ERROR = new ResStatus("401","Token已过期或未登录");
    public static ResStatus SPACE_NOT_ENOUGH_ERROR = new ResStatus("601","账户空间不足");
    private String code;
    private String msg;
    public ResStatus(String code, String msg){
        this.code = code;
        this.msg = msg;
    }
}
