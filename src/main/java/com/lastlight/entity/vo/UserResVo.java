package com.lastlight.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResVo {
    private Long uid;
    private String nickName;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime ;
    private Integer status;
    private Long userSpace;
    private Long totalSpace;
}
