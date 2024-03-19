package com.lastlight.controller;

import com.lastlight.annotation.*;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.ResetUserNameDto;
import com.lastlight.entity.dto.UserDto;
import com.lastlight.entity.vo.UserVo;
import com.lastlight.exception.CustomException;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserSecurity userSecurity;

    @GetMapping("")
    @GlobalInterceptor(checkLogin = true)
    public Result<UserVo> getByToken(@Token String token){
        UserVo user = userService.getByToken(token);
        if(user == null){
            throw new CustomException("未找到指定用户");
        }else
            return  Result.success(user);
    }

    @PostMapping("")
    @GlobalInterceptor(checkCode = true)
    public Result<String> save(@RequestBody @VerifyObjectParam @CodeObject UserDto userDto){
        userService.register(userDto);
        return Result.success(null);
    }

    @PostMapping("/login")
    @GlobalInterceptor(checkCode = true)
    public Result<UserVo> login(@RequestBody @CodeObject UserDto userDto){
        UserVo user = userService.login(userDto);
        return Result.success(user);
    }

    @PutMapping ("/reset-password")
    @GlobalInterceptor(checkCode = true)
    public Result<String> resetPwd(@RequestBody @CodeObject UserDto userDto){
        userService.resetPwd(userDto);
        return Result.success(null);
    }

    @PutMapping ("/reset-username")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> resetUsername(@RequestBody @TokenObject ResetUserNameDto dto){
        userService.resetUsername(dto);
        return Result.success(null);
    }

    @GetMapping("/avatar/{id}")
    public void getAvatar(@PathVariable String id, HttpServletResponse response){
        try {
            userService.getAvatar(Long.valueOf(id), response);
        }catch (NumberFormatException e){
            throw new CustomException("id格式有误");
        }
    }

    @PostMapping("/avatar")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> uploadAvatar(MultipartFile file, @Token String token){
        Long id = userSecurity.getId(token);
        userService.uploadAvatar(file,id);
        return Result.success(null);
    }

    @DeleteMapping("/logout")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> logOut(@Token String token){
        userService.logOut(token);
        return Result.success(null);
    }
}
