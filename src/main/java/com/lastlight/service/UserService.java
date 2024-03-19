package com.lastlight.service;

import com.lastlight.entity.User;
import com.lastlight.entity.dto.QueryDto;
import com.lastlight.entity.dto.ResetUserNameDto;
import com.lastlight.entity.dto.UserDto;
import com.lastlight.entity.vo.UserVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void add(String id, String password);
    void register(UserDto user);
    UserVo login(UserDto dto);
    User administratorLogin(UserDto dto);
    void resetPwd(UserDto userDto);
    void resetUsername(ResetUserNameDto dto);
    void getAvatar(Long id, HttpServletResponse response);
    void uploadAvatar(MultipartFile file, Long id);
    void logOut(String token);
    User getById(Long id);
    UserVo getByToken(String token);
    void updateUserSpace(Long uid, Long userSpace);
    void updateUserSpaceByOffset(Long uid, Long offset);
    void updateById(Long uid, String nickName, Integer status, Long totalSpace);
    User[] list(QueryDto dto);
}
