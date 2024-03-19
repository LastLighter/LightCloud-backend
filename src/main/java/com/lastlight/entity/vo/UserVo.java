package com.lastlight.entity.vo;

import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.common.RegularConstant;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {
    private String token;
    private Long uid;
    private String nickName;
    private String email;
    private String avatarName;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime ;
    private Integer status;
    private Long userSpace;
    private Long totalSpace;
}
