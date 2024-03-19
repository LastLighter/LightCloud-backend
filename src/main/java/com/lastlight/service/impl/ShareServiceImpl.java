package com.lastlight.service.impl;

import com.lastlight.common.FileConstant;
import com.lastlight.common.ShareConstant;
import com.lastlight.entity.FileEntity;
import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.*;
import com.lastlight.entity.vo.ShareAddVo;
import com.lastlight.entity.vo.ShareResVo;
import com.lastlight.exception.CustomException;
import com.lastlight.mapper.ShareMapper;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.FileService;
import com.lastlight.service.ShareService;
import com.lastlight.service.UserService;
import com.lastlight.utils.IDUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShareServiceImpl implements ShareService {
    @Autowired
    private ShareMapper shareMapper;
    @Autowired
    private UserSecurity userSecurity;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;

    @Override
    public ShareEntity[] listAll(ShareQueryDto queryDto) {
        return shareMapper.listAll(queryDto);
    }

    @Override
    public List<ShareResVo> list(ShareQueryDto queryDto) {
        if(queryDto.getOffset() == null) {
           queryDto.setOffset((queryDto.getPage() - 1) * queryDto.getPageSize());
        }
        Long id = userSecurity.getId(queryDto.getToken());
        queryDto.setUid(id);
        var list = shareMapper.list(queryDto);
        var voList = new ArrayList<ShareResVo>();
        for (ShareEntity shareEntity : list) {
            ShareResVo vo = new ShareResVo();
            FileEntity fileEntity = fileService.queryByFid(shareEntity.getFid());
            BeanUtils.copyProperties(fileEntity, vo);
            BeanUtils.copyProperties(shareEntity, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public ShareResVo add(ShareEntity shareEntity) {
        var shareId = IDUtil.getRandomId();
        shareEntity.setShareId(shareId);
        var code = IDUtil.getRandomId(4);
        shareEntity.setCode(code);
        LocalDateTime now = LocalDateTime.now();
        shareEntity.setCreateTime(now);
        LocalDateTime deadline = now.plusDays(shareEntity.getValidity());
        shareEntity.setExpiredTime(deadline);
        shareEntity.setView(ShareConstant.SHARE_VIEW_DEFAULT);

        shareMapper.save(shareEntity);
        ShareResVo vo = new ShareResVo();
        FileEntity fileEntity = fileService.queryByFid(shareEntity.getFid());
        BeanUtils.copyProperties(fileEntity, vo);
        BeanUtils.copyProperties(shareEntity, vo);
        return vo;
    }

    @Override
    public void delete(String shareId, String token) {
        Long id = userSecurity.getId(token);
        shareMapper.deleteByShareId(shareId, id);
    }

    @Override
    public ShareResVo getByCode(String shareId, String code) {
        ShareEntity shareEntity = shareMapper.getByCode(shareId, code);
        if(shareEntity == null){
            throw new CustomException("提取码错误或分享已取消");
        }
        ShareResVo dto = new ShareResVo();
        BeanUtils.copyProperties(shareEntity, dto);
        FileEntity fileEntity = fileService.queryByFid(dto.getFid());
        BeanUtils.copyProperties(fileEntity, dto);

        //update view
        shareMapper.updateView(shareEntity.getView() + 1, shareEntity.getShareId());

        return dto;
    }

    //内部获取数据接口
    public ShareResVo get(String shareId) {
        ShareEntity shareEntity = shareMapper.get(shareId);
        ShareResVo dto = new ShareResVo();
        BeanUtils.copyProperties(shareEntity, dto);
        FileEntity fileEntity = fileService.queryByFid(dto.getFid());
        BeanUtils.copyProperties(fileEntity, dto);

        return dto;
    }

    @Override
    public ShareInfoDto getInfo(String shareId) {
        ShareEntity shareEntity = shareMapper.get(shareId);
        if (shareEntity == null){
            throw new CustomException("分享链接不存在");
        }
//        FileEntity fileEntity = fileService.queryByFid(shareEntity.getFid());
        User user = userService.getById(shareEntity.getUid());
        ShareInfoDto shareInfoDto = new ShareInfoDto();
//        shareInfoDto.setAvatar(user.getAvatarName());
        shareInfoDto.setCreateTime(shareEntity.getCreateTime());
        shareInfoDto.setUserName(user.getNickName());
        shareInfoDto.setUid(user.getUid());
//        shareInfoDto.setFileName(fileEntity.getName());
//        shareInfoDto.setFid(fileEntity.getFid());

        return shareInfoDto;
    }

    @Override
    public List<FileDto> getFileList(String fid, String shareId) {
        ShareEntity shareEntity = shareMapper.get(shareId);
        if (shareEntity == null){
            throw new CustomException("分享链接不存在");
        }
        //校验请求的文件是否是share里的
        String curFid = fid;
        if (!fileService.isChildFile(shareEntity.getFid(), fid)) {
            throw new CustomException("非法操作");
        }

        FileQueryDto queryDto = new FileQueryDto();
        queryDto.defaultSize();
        queryDto.setFileParent(fid);
        FileEntity[] fileEntities = fileService.listByParent(queryDto);
        List<FileDto> dtoList = new ArrayList<>();
        for(FileEntity file: fileEntities){
            FileDto fileDto = new FileDto();
            BeanUtils.copyProperties(file, fileDto);
            dtoList.add(fileDto);
        }

        return dtoList;
    }

    @Override
    public void save(String shareId, String[] fidList, Long uid, String targetFid) {
        List<FileEntity> files = new ArrayList<>();
        for(String fid: fidList){
            FileEntity fileEntity = fileService.queryByFid(fid);
            files.add(fileEntity);
            ShareResVo shareResVo = get(shareId);
            boolean isChild = fileService.isChildFile(shareResVo.getFid(), fid);
            if(!isChild){
                throw CustomException.illegal();
            }
        }

        //校验目标文件是否为用户的文件夹
        FileEntity targetDir = fileService.queryByFid(targetFid);
        if(targetDir.getUserId() != uid || targetDir.getFolderType() == FileConstant.FILE_TYPE_NORMAL){
            throw CustomException.illegal();
        }

        List<FileEntity> tempFiles = new ArrayList<>();
        //获取全部子文件
        for(FileEntity item: files){
            List<FileEntity> subFiles = fileService.getSubFiles(item.getFid(), null);
            for(FileEntity subFile: subFiles){
                tempFiles.add(subFile);
            }
        }
        for(FileEntity item: tempFiles) {
            files.add(item);
        }

        //转存复制到指定目录
        for(FileEntity item: files){
            item.setFileParent(targetFid);
            item.setUserId(uid);
            //这里偷懒了，也就是说子文件也会保存到同一目录，原有目录结构不复存在
            fileService.defaultSave(item);
        }
    }
}
