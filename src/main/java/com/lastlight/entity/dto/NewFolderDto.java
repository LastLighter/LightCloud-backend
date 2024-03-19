package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import com.lastlight.annotation.VerifyBaseParam;
import lombok.Data;

@Data
public class NewFolderDto {
    @VerifyBaseParam(required = true)
    private  String fid;
    @VerifyBaseParam(required = true)
    private  String folderName;
    @Token
    private String token;
}
