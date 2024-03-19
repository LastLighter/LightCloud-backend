package com.lastlight.entity.dto;

import lombok.Data;

@Data
public class FileUploadResDto {
    public static final Integer UPLOADING = 0;
    public static final Integer UPLOAD_FINISH = 1;
    private String fileId;
    private Integer status;
}
