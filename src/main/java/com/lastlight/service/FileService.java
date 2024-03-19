package com.lastlight.service;

import com.lastlight.entity.FileEntity;
import com.lastlight.entity.dto.FileQueryDto;
import com.lastlight.entity.dto.FileUploadDto;
import com.lastlight.entity.dto.FileUploadResDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FileService {
    FileEntity[] listQueryByUserIdAndParent(FileQueryDto fileQueryDto);
    FileEntity[] listQueryDirByUserIdAndParent(FileQueryDto fileQueryDto);
    FileEntity[] listQueryByUserIdAndCategory(FileQueryDto fileQueryDto);
    FileEntity[] listQueryByMd5(FileQueryDto fileQueryDto);
    FileEntity[] listByParent(FileQueryDto fileQueryDto);
    Integer getSizeByParentAndCategory(Long uid, String fileParent, int fileCategory);
    FileEntity queryByFid(String fid);
    FileUploadResDto upload(FileUploadDto fileUploadDto);
    void getVideo(HttpServletResponse response,String fid, Long uid);
    void getTsFile(HttpServletResponse response,String fileName, Long uid);
    void getFile(HttpServletResponse response,String fid, Long uid);
    void newFolder(String name, String fid, Long uid);
    void rename(String name, String fid, Long uid);
    void move(String fids, String targetFid, Long uid);
    void download(HttpServletRequest request, HttpServletResponse response, String fid, Long uid);
    void delete(String fid, Long uid);
    void recycle(String fid, Long uid);
    void restore(String fid, Long uid);
    boolean isChildFile(String fid, String child);
    List<FileEntity> getSubFiles(String fid,  List<FileEntity> res);
    void defaultSave(FileEntity file);
    void save(FileEntity file);
    FileEntity[] listByStatusAndUID(Long uid, Integer status);
}
