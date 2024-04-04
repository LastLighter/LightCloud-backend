package com.lastlight.service.impl;

import com.lastlight.common.Constant;
import com.lastlight.common.FileConstant;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.FileEntity;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.FileQueryDto;
import com.lastlight.entity.dto.FileUploadDto;
import com.lastlight.entity.dto.FileUploadResDto;
import com.lastlight.entity.dto.QueryDto;
import com.lastlight.entity.vo.FileVo;
import com.lastlight.entity.vo.UserVo;
import com.lastlight.exception.CustomException;
import com.lastlight.exception.SpaceNotEnoughException;
import com.lastlight.mapper.FileMapper;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.FileService;
import com.lastlight.service.UserService;
import com.lastlight.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.lastlight.utils.FileUtil.merge;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private TempUserFileUtil tempUserFileUtil;
    @Autowired
    private PathUtil pathUtil;
    @Autowired
    private FFMPEGUtil ffmpegUtil;
    @Autowired
    private UserSecurity userSecurity;

    //因为无法直接调用异步方法（否则会失效），故引入，这样又要解决循环依赖问题。
    @Autowired
    @Lazy
    private FileServiceImpl fileService;

    @Override
    public FileEntity queryByFid(String fid) {
        return fileMapper.getByFid(fid);
    }

    @Override
    public FileEntity[] listQueryByUserIdAndParent(FileQueryDto fileQueryDto) {
        if(fileQueryDto.getOffset() == null) {
            fileQueryDto.setOffset((fileQueryDto.getPage() - 1) * fileQueryDto.getPageSize());
        }
        FileEntity[] files = fileMapper.getByUserIdAndParent(fileQueryDto);
        return files;
    }

    @Override
    public FileEntity[] listQueryDirByUserIdAndParent(FileQueryDto fileQueryDto) {
        if(fileQueryDto.getOffset() == null) {
            fileQueryDto.setOffset((fileQueryDto.getPage() - 1) * fileQueryDto.getPageSize());
        }
        return fileMapper.getDirByUserIdAndParent(fileQueryDto);
    }

    @Override
    public FileVo[] listQuery(QueryDto dto) {
        if(dto.getOffset() == null) {
            dto.setOffset((dto.getPage() - 1) * dto.getPageSize());
        }
        return fileMapper.get(dto);
    }

    @Override
    public FileEntity[] listQueryByMd5(FileQueryDto fileQueryDto) {
        return fileMapper.getByMd5(fileQueryDto);
    }

    @Override
    public FileEntity[] listQueryByUserIdAndCategory(FileQueryDto fileQueryDto) {
        if(fileQueryDto.getOffset() == null) {
            fileQueryDto.setOffset((fileQueryDto.getPage() - 1) * fileQueryDto.getPageSize());
        }
        return fileMapper.getByUserIdAndCategory(fileQueryDto);
    }

    @Override
    @Transactional
    public FileUploadResDto upload(FileUploadDto fileUploadDto) {
        Boolean success = true;
        try {
            FileUploadResDto resDto = new FileUploadResDto();
            UserVo user = userService.getByToken(fileUploadDto.getToken());

            if (fileUploadDto.getChunkIndex() == 0) {
                //the first chunk
                String fid = IDUtil.getRandomId();
                resDto.setFileId(fid);
                fileUploadDto.setFileId(fid);
                String md5 = fileUploadDto.getMd5();
                FileEntity[] md5Files = listQueryByMd5(new FileQueryDto(1, 0, md5));

                //mybatis中如果查询无结果，回返回一个空数组，而非null值。
                if (md5Files.length > 0) {
                    //there is same file in database
                   fastUpload(md5Files, fileUploadDto, user, fid);

                    //upload finish
                    resDto.setStatus(FileUploadResDto.UPLOAD_FINISH);
                    //update current userSpace
                    userService.updateUserSpace(user.getUid(), user.getUserSpace() + md5Files[0].getFileSize());
                    return resDto;
                }
                //添加文件临时空间
                tempUserFileUtil.setTempSpace(resDto.getFileId(), 0L);
            }

            if (resDto.getFileId() == null) {
                resDto.setFileId(fileUploadDto.getFileId());
            }
            if (fileUploadDto.getFile().getSize() + tempUserFileUtil.getTempSpace(resDto.getFileId()) + user.getUserSpace() > user.getTotalSpace()) {
                throw new SpaceNotEnoughException("用户空间不足");
            }
            tempUserFileUtil.write(fileUploadDto.getFile(), resDto.getFileId(), fileUploadDto.getChunkIndex());

            if (fileUploadDto.getChunkIndex() < fileUploadDto.getChunkNum() - 1) {
                resDto.setStatus(FileUploadResDto.UPLOADING);
            } else if (fileUploadDto.getChunkIndex() == fileUploadDto.getChunkNum() - 1) {
                //最后一片
                finalUpload(fileUploadDto,user);
                resDto.setStatus(FileUploadResDto.UPLOAD_FINISH);

                Long tempSpace = tempUserFileUtil.getTempSpace(resDto.getFileId());

                //update current userSpace
                userService.updateUserSpace(user.getUid(), user.getUserSpace() + tempSpace);

                //异步合并文件
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        fileService.transferFile(resDto.getFileId());
                    }
                });
            } else {
                throw new CustomException("上传出错");
            }
            return resDto;
        }catch (Exception e){
            success = false;
            throw e;
        }finally {
            if(!success){
                tempUserFileUtil.deleteFile(fileUploadDto.getFileId());
            }
        }
    }

    private void fastUpload(FileEntity[] md5Files, FileUploadDto fileUploadDto, UserVo user, String fid){
        FileEntity md5File = md5Files[0];
        FileEntity fileEntity = new FileEntity();
        String name = fileUploadDto.getFile().getOriginalFilename();
        String suffix = name.substring(name.indexOf('.') + 1);
        Integer category = FileUtil.getCategory(suffix);
        if (md5File.getFileSize() + user.getUserSpace() > user.getTotalSpace()) {
            throw new SpaceNotEnoughException("用户空间不足");
        }
        //秒传
        fileEntity.setFid(fid);
        String fileName = fileUploadDto.getFile().getOriginalFilename();
        if (fileName.length() >= 30) {
            fileName = fileName.substring(0,30) + '.' + suffix;
        }
        fileEntity.setName(fileName);
        fileEntity.setFilePath(md5File.getFilePath());
        fileEntity.setUserId(user.getUid());
        fileEntity.setFileMd5(md5File.getFileMd5());
        fileEntity.setFileParent(fileUploadDto.getFileParentId());
        fileEntity.setFileSize(md5File.getFileSize());
        //todo the cover of file
        fileEntity.setFileCategory(category);

        fileEntity.setFolderType(FileConstant.FILE_TYPE_NORMAL);
        fileEntity.setStatus(FileConstant.FILE_STATUS_NORMAL);
        fileEntity.setCreateTime(LocalDateTime.now());
        fileEntity.setLastUpdateTime(LocalDateTime.now());
        save(fileEntity);
    }

    private void finalUpload(FileUploadDto fileUploadDto, UserVo user){
        String name = fileUploadDto.getFile().getOriginalFilename();
        String suffix = name.substring(name.indexOf('.') + 1);
        Integer category = FileUtil.getCategory(suffix);

        //add file in database
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFid(fileUploadDto.getFileId());
        String fileName = name;
        if (name.length() >= 30) {
            fileName = name.substring(0,30) + '.' + suffix;
        }
        fileEntity.setName(fileName);
        String userFileFolderPath = pathUtil.getUserFileFolderPath(LocalDateTime.now());
        //获取其倒数第三级开始的目录
        String reactivePath = PathUtil.getLastPath(userFileFolderPath, 3);
        String path = reactivePath + File.separator + fileUploadDto.getFileId() + '.' + suffix;
        fileEntity.setFilePath(path);

        fileEntity.setUserId(user.getUid());
        fileEntity.setFileMd5(fileUploadDto.getMd5());
        fileEntity.setFileParent(fileUploadDto.getFileParentId());
        fileEntity.setFileSize(tempUserFileUtil.getTempSpace(fileUploadDto.getFileId()));
        fileEntity.setFileCategory(category);
        //todo the cover of file

        fileEntity.setFolderType(FileConstant.FILE_TYPE_NORMAL);
        fileEntity.setStatus(FileConstant.FILE_STATUS_NORMAL);
        fileEntity.setCreateTime(LocalDateTime.now());
        fileEntity.setLastUpdateTime(LocalDateTime.now());
        try {
            save(fileEntity);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Async
    public void transferFile(String fid){
        FileEntity fileEntity = queryByFid(fid);
        //筛选所有的fid开头的文件
        File dir = new File(pathUtil.getTempFolderPath());
        File[] files = dir.listFiles((d, name) -> name.startsWith(fid));
        //先排好序再合并
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int index1 = o1.getName().indexOf('-');
                Integer order1 = Integer.valueOf(o1.getName().substring(index1 + 1));
                int index2 = o1.getName().indexOf('-');
                Integer order2 = Integer.valueOf(o2.getName().substring(index2 + 1));
                return order1 - order2;
            }
        });


        String filePath = pathUtil.getUserFileFolderBasePath() + File.separatorChar + fileEntity.getFilePath();
        FileUtil.merge(files, filePath);

        //根据文件类型处理
        String suffix = FileUtil.getSuffix(fileEntity.getName());
        if(fileEntity.getFileCategory() == FileConstant.FILE_CATEGORY_VIDEO){
//            ffmpegUtil.cutVideoToTs(filePath);
//            ffmpegUtil.screenShot(filePath);
        } else if (fileEntity.getFileCategory() == FileConstant.FILE_CATEGORY_IMAGE) {

        }

        //删除临时文件
        tempUserFileUtil.deleteFile(fid);
    }

    @Override
    public void getVideo(HttpServletResponse response, String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        //校验文件是否为该用户所有
        if(uid != fileEntity.getUserId()){
            throw new CustomException("文件不存在");
        }
        String m3u8Path = FileUtil.getNoSuffixPath(fileEntity.getFilePath()) + Constant.M3U8_FILE_NAME;

        File m3u8File = new File(pathUtil.getUserFileFolderBasePath() + m3u8Path);
        if(!m3u8File.exists()){
            throw new CustomException("视频文件不存在");
        }
        try {
            FileUtil.writeToResponse(m3u8File, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getTsFile(HttpServletResponse response, String fileName, Long uid) {
        String[] strings = fileName.split("_");
        String fid = strings[0];
        String fName = strings[1];
        FileEntity fileEntity = queryByFid(fid);
        //校验文件是否为该用户所有
        if(uid != fileEntity.getUserId()){
            throw new CustomException("文件不存在");
        }

        if(fileEntity == null){
            throw new CustomException("视频转码文件不存在");
        }
        String filePath = fileEntity.getFilePath();
        String tsPath = FileUtil.getNoSuffixPath(filePath) + File.separatorChar + fName;
        File tsFile = new File(tsPath);
        if(!tsFile.exists()){
            throw new CustomException("视频转码文件不存在");
        }
        try {
            FileUtil.writeToResponse(tsFile, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getFile(HttpServletResponse response, String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if(fileEntity.getUserId() == uid){
            String path = pathUtil.getUserFileFolderBasePath() + File.separatorChar + fileEntity.getFilePath();
            try {
                FileUtil.writeToResponse(new File(path), response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CustomException("无权限操作");
    }

    @Override
    public FileEntity[] search(String keyword, Long uid) {
        return fileMapper.listByKeyword(uid, keyword);
    }

    @Override
    public void newFolder(String name, String fid, Long uid) {
        //查询是否已存在同名目录
        FileQueryDto queryDto = new FileQueryDto();
        queryDto.setName(name);
        queryDto.setFileParent(fid);
        Integer count = fileMapper.countByFileNameAndParent(queryDto);
        if(count > 0){
            throw new CustomException("已存在同名文件");
        }

        FileEntity file = new FileEntity();
        file.setFid(IDUtil.getRandomId());
        file.setName(name);
        file.setUserId(uid);
        file.setFileParent(fid);
        file.setFolderType(FileConstant.FILE_TYPE_DIR);
        file.setStatus(FileConstant.FILE_STATUS_NORMAL);
        file.setCreateTime(LocalDateTime.now());
        file.setLastUpdateTime(LocalDateTime.now());

        fileMapper.add(file);
    }

    @Override
    public void rename(String name, String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if (fileEntity == null) {
            throw new CustomException("文件不存在");
        }else if(fileEntity.getUserId() != uid){
            throw new CustomException("无权限操作");
        }
        String originalName = fileEntity.getName();
        String suffix = FileUtil.getSuffix(originalName);

        FileQueryDto queryDto = new FileQueryDto();
        queryDto.setFileParent(fid);
        queryDto.setName(name + suffix);
        Integer count = fileMapper.countByFileNameAndParent(queryDto);
        if(count > 0){
            throw new CustomException("文件名已存在");
        }
        fileMapper.rename(name, fid);
    }

    @Override
    @Transactional
    public void move(String fid, String targetFid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if(fileEntity.getUserId() != uid){
            throw new CustomException("非法文件操作");
        }

        //查询文件名是否重复
        FileQueryDto queryDto = new FileQueryDto();
        queryDto.setPage(1);
        queryDto.setPageSize(FileQueryDto.MAX_PAGE_SIZE);
        queryDto.setFileParent(targetFid);
        queryDto.setUserId(uid);
        FileEntity[] targetDirFiles = listQueryByUserIdAndParent(queryDto);
        for(FileEntity item: targetDirFiles){
            if (fileEntity.getName().equals(item.getName())) {
                //重命名
                fileMapper.rename(fileEntity.getName() + IDUtil.getRandomId(), fileEntity.getFid());
            }
        }
        fileMapper.move(targetFid, fid);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String fid, Long uid) {
        FileEntity fileEntity = fileService.queryByFid(fid);
        if (fileEntity == null || fileEntity.getUserId() != uid) {
            throw new CustomException("文件无法访问");
        }
        String path = pathUtil.getUserFileFolderBasePath() + File.separatorChar + fileEntity.getFilePath();
        File file = new File(path);

        String fileName = "";
        try {
            //文件名需要URL编码，不然下载后的文件名会出问题
            fileName = URLEncoder.encode(fileEntity.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("application/octet-stream");

        boolean isRangeRequest = false;
        long start=0,end=fileEntity.getFileSize();
        //断点续传Range字段判断
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            isRangeRequest = true;
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes=" + start + "-" + end + "/" + fileEntity.getFileSize());
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

        try {
            if(isRangeRequest) {
                FileUtil.sliceWriteToResponse(response, start, end, file, Integer.valueOf(2 * Constant.MB));
            }else{
                FileUtil.writeToResponse(file, response);
            }
        } catch (Exception e) {
            throw new CustomException("文件下载失败");
        }
    }

    @Override
    @Transactional
    public void delete(String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if (fileEntity == null || fileEntity.getUserId() != uid) {
            throw new CustomException("无权限操作");
        }

        if(fileEntity.getFolderType() == FileConstant.FILE_TYPE_DIR){
            //彻底删除
            delete(fileEntity);

            List<FileEntity> subFiles = getSubFiles(fid, null);
            Long fileSize = 0L;
            for(FileEntity item: subFiles){
                //删除子文件数据
                fileMapper.deleteByFid(item.getFid());
                //统计子文件体积
                if(item.getFileSize() != null){
                    fileSize += item.getFileSize();
                }
            }
            //释放空间
            User user = userService.getById(uid);
            userService.updateUserSpaceByOffset(uid, fileSize);

            //删除数据
            fileMapper.deleteByFid(fileEntity.getFid());
            return;
        }
        //释放空间
        User user = userService.getById(uid);
        userService.updateUserSpace(uid, user.getUserSpace() - fileEntity.getFileSize());

        //彻底删除文件（前提是其他文件中没有存在同样的本地文件引用）
        delete(fileEntity);

        //删除数据(一定要先删除本地文件在删除数据，因为删除校验时会统计数据库的引用数）
        fileMapper.deleteByFid(fileEntity.getFid());
    }

    private void delete(FileEntity fileEntity){
        //如果不是文件夹
        if(fileEntity.getFilePath() != null){
            //统计是否多个文件引用了它（那样当然不能删除）
            Integer size = fileMapper.countByFilePath(fileEntity.getFilePath());
            if(size > 1){
                return;
            }

            String path = pathUtil.getUserFileFolderBasePath() + File.separatorChar + fileEntity.getFilePath();
            File file = new File(path);
            if(!file.delete()){
                throw new CustomException("用户文件删除失败");
            }
            return;
        }

        //search child file
        FileEntity[] childFiles = fileMapper.getByFileParent(fileEntity.getFid());
        for(FileEntity childFile: childFiles){
            delete(childFile);
        }
    }

    @Override
    public void recycle(String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if (fileEntity == null || fileEntity.getUserId() != uid) {
            throw new CustomException("无权限操作");
        }
        fileMapper.changeStatus(FileConstant.FILE_STATUS_RECOVERY, fid);
    }

    @Override
    public void restore(String fid, Long uid) {
        FileEntity fileEntity = queryByFid(fid);
        if (fileEntity == null || fileEntity.getUserId() != uid) {
            throw new CustomException("无权限操作");
        }
        //todo rename if dir has a file with same name.
        fileMapper.changeStatus(FileConstant.FILE_STATUS_NORMAL, fid);
        //set the parent as the root dir
        fileMapper.move(FileConstant.FILE_PARENT_ROOT, fid);
    }

    @Override
    public FileEntity[] listByParent(FileQueryDto fileQueryDto) {
        FileEntity[] list = fileMapper.getByParent(fileQueryDto);
        return list;
    }

    @Override
    public Integer getSizeByParentAndCategory(Long uid, String fileParent, int fileCategory) {
        if(fileCategory != 0) {
            return fileMapper.countByParentAndCategory(uid, fileParent, fileCategory);
        }
        return fileMapper.countByParent(uid, fileParent);
    }

    @Override
    public boolean isChildFile(String fid, String childFid) {
        while(true){
            if(fid.equals(childFid)){
                break;
            }
            FileEntity curFile = queryByFid(childFid);
            if(curFile.getFileParent() == FileConstant.FILE_PARENT_ROOT){
                return false;
            }
            childFid = curFile.getFileParent();
        }
        return true;
    }

    @Override
    public List<FileEntity> getSubFiles(String fid, List<FileEntity> res) {
        if (res == null) {
            res = new ArrayList<>();
        }

        FileEntity[] children = fileMapper.getByFileParent(fid);
        for(FileEntity item: children){
            if(item.getStatus() == FileConstant.FILE_STATUS_NORMAL) {
                res.add(item);
                if(item.getFolderType() == FileConstant.FILE_TYPE_DIR) {
                    getSubFiles(item.getFid(), res);
                }
            }
        }

        return res;
    }

    @Override
    public void save(FileEntity file) {
        fileMapper.add(file);
    }

    @Override
    public void defaultSave(FileEntity file) {
        file.setFid(IDUtil.getRandomId());
        file.setStatus(FileConstant.FILE_STATUS_NORMAL);
        file.setCreateTime(LocalDateTime.now());
        file.setLastUpdateTime(LocalDateTime.now());
        fileMapper.add(file);
    }

    @Override
    public FileEntity[] listByStatusAndUID(Long uid, Integer status) {
        return fileMapper.listByStatusAndUID(uid, status);
    }

    @Override
    public Integer getSize() {
        return fileMapper.getSize();
    }
}
