package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import lombok.Data;

@Data
public class ShareSaveDto {
    @Token
    private String token;
    private String shareId;
    private String fidList;
    private String targetFid;
}
