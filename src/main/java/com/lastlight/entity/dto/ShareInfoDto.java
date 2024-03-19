package com.lastlight.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareInfoDto {
//    private String fid;
    private String userName;
    private LocalDateTime createTime;
//    private String avatar;
//    private String fileName;
    private Long uid;
}
