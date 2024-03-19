package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import lombok.Data;

@Data
public class ResetUserNameDto {
    @Token
    private String token;
    private String name;
}
