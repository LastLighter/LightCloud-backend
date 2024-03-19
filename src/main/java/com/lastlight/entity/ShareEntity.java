package com.lastlight.entity;

import com.lastlight.annotation.VerifyBaseParam;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareEntity {
    private String shareId;
    @VerifyBaseParam(required = true)
    private String fid;
    private Long uid;
    @VerifyBaseParam(required = true)
    private Integer validity;
    private LocalDateTime createTime;
    private LocalDateTime expiredTime;
    private String code;
    private Integer view;
}
