package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import lombok.Data;

@Data
public class UpdateUserDto {
    private Long uid;
    private String nickName;
    private Integer status;
    private Long totalSpace;
    @Token
    private String token;
}
