package com.lastlight.controller;

import com.lastlight.annotation.CodeObject;
import com.lastlight.annotation.GlobalInterceptor;
import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.common.Constant;
import com.lastlight.config.EmailConfig;
import com.lastlight.config.RuntimeConfig;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.QueryDto;
import com.lastlight.entity.dto.ShareQueryDto;
import com.lastlight.entity.dto.SysSettingDto;
import com.lastlight.entity.dto.UserDto;
import com.lastlight.service.ShareService;
import com.lastlight.service.UserService;
import com.lastlight.utils.ConfigUtil;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/administration")
public class AdministrationController {
    @Autowired
    private ShareService shareService;
    @Autowired
    private UserService userService;
    @Autowired
    private RuntimeConfig runtimeConfig;

    //管理员查询分享列表
    @GetMapping("/listShare")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<ShareEntity[]> listShare(ShareQueryDto queryDto){
        return Result.success(shareService.listAll(queryDto));
    }

    @PostMapping("/administration/login")
    @GlobalInterceptor(checkCode = true)
    public Result<User> administratorLogin(@RequestBody @CodeObject UserDto userDto){
        User res = userService.administratorLogin(userDto);
        return Result.success(res);
    }

    @GetMapping("/listUser")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<User[]> listUser(QueryDto dto){
        return Result.success(userService.list(dto));
    }

    @PutMapping("/setting")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<String> updateSetting(SysSettingDto settingDto){
        ConfigUtil.setSysSetting(runtimeConfig, settingDto);
        return Result.success(null);
    }

    @GetMapping("/setting")
    @GlobalInterceptor(checkAdministrator = true)
    public Result<SysSettingDto> getSetting(){
        return Result.success(ConfigUtil.getSysSetting(runtimeConfig));
    }

    @PutMapping("/user")
    public Result<String> updateUser(Long uid, String nickName, Integer status, Long totalSpace){
        userService.updateById(uid, nickName, status, totalSpace);
        return Result.success(null);
    }
}
