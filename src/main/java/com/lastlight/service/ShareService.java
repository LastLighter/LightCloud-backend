package com.lastlight.service;

import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.dto.FileDto;
import com.lastlight.entity.dto.ShareInfoDto;
import com.lastlight.entity.dto.ShareQueryDto;
import com.lastlight.entity.vo.ShareAddVo;
import com.lastlight.entity.vo.ShareResVo;

import java.util.List;

public interface ShareService {
    ShareEntity[] listAll(ShareQueryDto queryDto);
    List<ShareResVo> list(ShareQueryDto queryDto);
    ShareResVo add(ShareEntity shareEntity);
    void delete(String shareId, String token);
    ShareResVo getByCode(String shareId, String code);
    ShareInfoDto getInfo(String shareId);
    List<FileDto> getFileList(String fid, String shareId);
    void save(String shareId, String[] fidList,Long uid, String targetFid);
}
