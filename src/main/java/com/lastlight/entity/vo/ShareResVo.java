package com.lastlight.entity.vo;

import com.lastlight.entity.ShareEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareResVo extends ShareEntity {
    private String name;
    private Integer folderType;
    private Integer fileCategory;
    private Long fileSize;
}
