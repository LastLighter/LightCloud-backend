package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import com.lastlight.annotation.VerifyBaseParam;
import lombok.Data;

@Data
public class FileMoveDto {
    @VerifyBaseParam(required = true)
    private String fids;
    @VerifyBaseParam(required = true)
    private String targetFid;
    @Token
    private String token;
}
