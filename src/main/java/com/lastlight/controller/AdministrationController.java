package com.lastlight.controller;

import com.lastlight.annotation.*;
import com.lastlight.common.Constant;
import com.lastlight.config.EmailConfig;
import com.lastlight.config.RuntimeConfig;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.FileEntity;
import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.*;
import com.lastlight.entity.vo.UserResVo;
import com.lastlight.entity.vo.UserVo;
import com.lastlight.service.FileService;
import com.lastlight.service.ShareService;
import com.lastlight.service.UserService;
import com.lastlight.utils.ConfigUtil;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/administration")
public class AdministrationController {
    @Autowired
    private ShareService shareService;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private RuntimeConfig runtimeConfig;

    //管理员查询分享列表
    @GetMapping("/listShare")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<ShareEntity[]> listShare(ShareQueryDto queryDto){
        return Result.success(shareService.listAll(queryDto));
    }

    @PostMapping("/login")
    @GlobalInterceptor(checkCode = true)
    public Result<UserVo> administratorLogin(@RequestBody @CodeObject UserDto userDto){
        UserVo res = userService.administratorLogin(userDto);
        return Result.success(res);
    }

    @GetMapping("/listUser")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<List<UserResVo>> listUser(QueryDto dto, @Token String token){
        return Result.success(userService.list(dto));
    }

    @GetMapping("/getUserSize")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<Integer> listUserSize(@Token String token){
        return Result.success(userService.getSize());
    }

    @GetMapping("/listFile")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<FileEntity[]> listFile(QueryDto dto, @Token String token){
        return Result.success(fileService.listQuery(dto));
    }

    @GetMapping("/getFileSize")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<Integer> listFileSize(@Token String token){
        return Result.success(fileService.getSize());
    }

    @DeleteMapping("/delete/{fid}")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<String> delete(@Token String token, Long uid, @PathVariable String fid){
        fileService.delete(fid, uid);
        return Result.success(null);
    }

    @PutMapping("/setting")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<String> updateSetting(@RequestBody SysSettingDto settingDto, @Token String token){
        ConfigUtil.setSysSetting(runtimeConfig, settingDto);
        return Result.success(null);
    }

    @GetMapping("/setting")
    @GlobalInterceptor()
    public Result<SysSettingDto> getSetting(){
        return Result.success(ConfigUtil.getSysSetting(runtimeConfig));
    }

    @PutMapping("/user")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<String> updateUser(@RequestBody @TokenObject UpdateUserDto dto){
        userService.updateById(dto.getUid(), dto.getNickName(), dto.getStatus(), dto.getTotalSpace());
        return Result.success(null);
    }
}
