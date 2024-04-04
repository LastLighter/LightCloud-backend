package com.lastlight.controller;

import com.lastlight.annotation.*;
import com.lastlight.common.FileConstant;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.FileEntity;
import com.lastlight.entity.dto.*;
import com.lastlight.exception.CustomException;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.FileService;
import com.lastlight.utils.IDUtil;
import com.lastlight.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static com.lastlight.utils.IDUtil.getUUID;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private UserSecurity userSecurity;

    @GetMapping("/list")
    public Result<FileEntity[]> list(FileQueryDto fileQueryDto){
        //分页查询
        FileEntity[] fileEntities = fileService.listQueryByUserIdAndParent(fileQueryDto);
        return Result.success(fileEntities);
    }

    @GetMapping("/listDir")
    public Result<FileEntity[]> listDir(FileQueryDto fileQueryDto){
        //分页查询
        FileEntity[] fileEntities = fileService.listQueryDirByUserIdAndParent(fileQueryDto);
        return Result.success(fileEntities);
    }

    @GetMapping("/listByCategory")
    public Result<FileEntity[]> listByCategory(FileQueryDto fileQueryDto){
        //分页查询
        FileEntity[] fileEntities = fileService.listQueryByUserIdAndCategory(fileQueryDto);
        return Result.success(fileEntities);
    }

    @GetMapping("/getSize")
    @GlobalInterceptor(checkLogin = true)
    public Result<Integer> getSize(@Token String token, String fileParent, int fileCategory){
        Long id = userSecurity.getId(token);
        return Result.success(fileService.getSizeByParentAndCategory(id, fileParent, fileCategory));
    }

    @PostMapping("/upload")
    public Result<FileUploadResDto> upload(@ModelAttribute FileUploadDto fileUploadDto){
//        fileUploadDto.setFile(file);
        FileUploadResDto resDto = fileService.upload(fileUploadDto);
        return Result.success(resDto);
    }

    @GetMapping("/{fid}")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public void getFile(HttpServletResponse response, @PathVariable @VerifyBaseParam(required = true) String fid, String category, @Token String token){
        Long uid = userSecurity.getId(token);
        switch (category) {
            case "video" -> fileService.getVideo(response, fid, uid);
            //这个fid要求告诉我们是哪个视频文件的ts，格式类似于fid_0000.ts
            case "ts" -> fileService.getTsFile(response, fid, uid);
            case "other" -> fileService.getFile(response, fid, uid);
            default -> throw new CustomException("文件类型不存在");
        }
    }

    @GetMapping("/search")
    @GlobalInterceptor(checkLogin = true)
    public Result<FileEntity[]> searchFile(String keyword, @Token String token){
        Long uid = userSecurity.getId(token);
        FileEntity[] fileEntities = fileService.search(keyword, uid);
        if(fileEntities == null){
            return Result.success(new FileEntity[0]);
        }
        return Result.success(fileEntities);
    }

    @PostMapping("/newFolder")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public Result<String> newFolder(@RequestBody NewFolderDto dto){
        Long uid = userSecurity.getId(dto.getToken());
        fileService.newFolder(dto.getFolderName(), dto.getFid(), uid);

        return Result.success(null);
    }

    @PutMapping("/rename")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public Result<String> rename(@RequestBody @VerifyObjectParam @TokenObject RenameDto dto){
        Long uid = userSecurity.getId(dto.getToken());
        fileService.rename(dto.getFileName(), dto.getFid(), uid);

        return Result.success(null);
    }

    @PutMapping("/move")
    @GlobalInterceptor
    //fids是文件的批量id，表示要移动的全部文件，文件id之间用下划线分隔
    public Result<String> move(@RequestBody @Token @VerifyObjectParam FileMoveDto dto){
        Long uid = userSecurity.getId(dto.getToken());
        fileService.move(dto.getFids(), dto.getTargetFid(), uid);

        return Result.success(null);
    }

    @GetMapping("/download/{fid}")
    @GlobalInterceptor
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable String fid, Long uid){
        fileService.download(request, response, fid, uid);
    }

    @DeleteMapping("/delete/{fid}")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> delete(@Token String token, @PathVariable String fid){
        Long uid = userSecurity.getId(token);
        fileService.delete(fid, uid);
        return Result.success(null);
    }

    @GetMapping("/recycle")
    @GlobalInterceptor(checkLogin = true)
    public Result<FileEntity[]> list(@Token String token){
        Long id = userSecurity.getId(token);
        FileEntity[] fileEntities = fileService.listByStatusAndUID(id, FileConstant.FILE_STATUS_RECOVERY);
        return Result.success(fileEntities);
    }

    @PutMapping("/recycle/{fid}")
    @GlobalInterceptor
    public Result<String> recycle(@Token String token, @PathVariable String fid){
        Long uid = userSecurity.getId(token);
        fileService.recycle(fid, uid);
        return Result.success(null);
    }

    @PutMapping("/restore/{fid}")
    @GlobalInterceptor
    public Result<String> restore(@Token String token, @PathVariable String fid){
        Long uid = userSecurity.getId(token);
        fileService.restore(fid, uid);
        return Result.success(null);
    }
}
