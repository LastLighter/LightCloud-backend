package com.lastlight.controller;

import com.lastlight.annotation.*;
import com.lastlight.common.Constant;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.FileEntity;
import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.dto.FileDto;
import com.lastlight.entity.dto.ShareInfoDto;
import com.lastlight.entity.dto.ShareQueryDto;
import com.lastlight.entity.dto.ShareSaveDto;
import com.lastlight.entity.vo.ShareAddVo;
import com.lastlight.entity.vo.ShareResVo;
import com.lastlight.exception.CustomException;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.FileService;
import com.lastlight.service.ShareService;
import com.lastlight.utils.IDUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/share")
public class ShareController {
    @Autowired
    private ShareService shareService;
    @Autowired
    private UserSecurity userSecurity;
    @Autowired
    private FileService fileService;


    /*
    针对特定用户的查询
     */
    @GetMapping("/list")
    public Result<List<ShareResVo>> list(ShareQueryDto queryDto){
        return Result.success(shareService.list(queryDto));
    }

    @PostMapping("/add")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public Result<ShareResVo> add(@VerifyObjectParam ShareEntity shareEntity, @Token String token){
        Long id = userSecurity.getId(token);
        shareEntity.setUid(id);
        FileEntity fileEntity = fileService.queryByFid(shareEntity.getFid());
        if(id != fileEntity.getUserId()){
            throw new CustomException("无权限操作");
        }
        //默认应该携带fid、uid、和validity
        return Result.success(shareService.add(shareEntity));
    }

    @DeleteMapping("/cancel")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public Result<String> cancel(@VerifyBaseParam(required = true) String shareId, @Token String token){
        shareService.delete(shareId, token);
        return Result.success(null);
    }

    @GetMapping("/get/{shareId}")
    @GlobalInterceptor(checkParams = true)
    public Result<ShareResVo> get(@PathVariable @VerifyBaseParam(required = true)String shareId, @VerifyBaseParam(required = true)String code){
        ShareResVo vo = shareService.getByCode(shareId, code);
        return Result.success(vo);
    }

    @GetMapping("/getInfo/{shareId}")
    @GlobalInterceptor(checkParams = true)
    public Result<ShareInfoDto> getInfo(@PathVariable @VerifyBaseParam(required = true) String shareId){
        return Result.success(shareService.getInfo(shareId));
    }

    //好像没必要的样子，直接文件的接口就行了
    @GetMapping("/getFileList/{fid}")
    @GlobalInterceptor(checkParams = true)
    public Result<List<FileDto>> getFileList(@PathVariable @VerifyBaseParam(required = true) String fid, @VerifyBaseParam(required = true) String shareId){
        return Result.success(shareService.getFileList(fid, shareId));
    }

    @PostMapping("/save")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public Result<String> save(@RequestBody @TokenObject ShareSaveDto dto){
        Long id = userSecurity.getId(dto.getToken());
        String[] fids = dto.getFidList().split(Constant.GLOBAL_SEPERATOR);
        shareService.save(dto.getShareId(),fids,id,dto.getTargetFid());

        return Result.success(null);
    }
}
