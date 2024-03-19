package com.lastlight.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileEntity {
    private String fid;
    private String name;
    private String filePath;
    private Long userId;
    private String fileMd5;
    private String fileParent;
    private Long fileSize;
    private String fileCover;
    private Integer folderType;
    private Integer fileCategory;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastUpdateTime;
    private LocalDateTime recoveryTime;
}
