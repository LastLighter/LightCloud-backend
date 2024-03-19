package com.lastlight.entity.dto;

import com.lastlight.annotation.Code;
import com.lastlight.annotation.CodeKey;
import com.lastlight.annotation.Token;
import com.lastlight.entity.User;
import lombok.Data;

@Data
public class UserDto extends User {
    @CodeKey
    private String codeKey;
    @Code
    private String code;
    @Token
    private String token;
}
