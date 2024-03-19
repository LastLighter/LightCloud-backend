package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShareQueryDto extends QueryDto{
    @Token
    private String token;
    private Long uid;
}
