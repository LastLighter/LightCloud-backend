package com.lastlight.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadDto {
    private String fileId;
    private String fileParentId;
    private String md5;
    private Integer chunkIndex;
    private Integer chunkNum;
    private String token;
    private MultipartFile file;
}
