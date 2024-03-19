package com.lastlight.entity;

import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.common.RegularConstant;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    @VerifyBaseParam(max=15)
    private String nickName;
    @VerifyBaseParam(required = true, max=150, regEx = RegularConstant.REG_EMAIL)
    private String email;
    private String qqOpenId ;
    private String qqAvatar ;
    @VerifyBaseParam(required = true, regEx = RegularConstant.REG_PASSWORD)
    private String password;
    private String avatarName;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime ;
    private Integer status;
    private Long userSpace;
    private Long totalSpace;
}
