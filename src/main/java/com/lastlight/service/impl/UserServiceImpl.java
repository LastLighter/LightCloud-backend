package com.lastlight.service.impl;

import com.lastlight.common.Constant;
import com.lastlight.config.AppConfig;
import com.lastlight.config.RuntimeConfig;
import com.lastlight.entity.User;
import com.lastlight.entity.dto.QueryDto;
import com.lastlight.entity.dto.ResetUserNameDto;
import com.lastlight.entity.dto.UserDto;
import com.lastlight.entity.vo.UserResVo;
import com.lastlight.entity.vo.UserVo;
import com.lastlight.exception.CustomException;
import com.lastlight.mapper.UserMapper;
import com.lastlight.security.UserSecurity;
import com.lastlight.service.UserService;
import com.lastlight.utils.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RuntimeConfig runtimeConfig;
    @Autowired
    private UserSecurity userSecurity;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PathUtil pathUtil;

    @Override
    public User getById(Long id) {
        User user = userMapper.getById(id);
        return user;
    }

    @Override
    public UserVo getByToken(String token) {
        Long id = userSecurity.getId(token);
        User user = userMapper.getById(id);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public void add(String id, String password){
        userMapper.add(id,password);
    }

    @Override
    @Transactional
    public void register(UserDto userDto){
        //check if the email has been used
        if(userMapper.getByEmail(userDto.getEmail()) != null)
            throw new CustomException("邮箱已被注册");

        //set field
        User user = new User();
        user.setEmail(userDto.getEmail());
        //encoded with md5
        user.setPassword(StringUtil.encodeWithMd5(userDto.getPassword()));
        user.setNickName(userDto.getNickName());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setTotalSpace(Integer.valueOf(runtimeConfig.get(Constant.BASE_SPACE)) * Constant.MB.longValue());
        user.setUserSpace(0L);

        userMapper.register(user);
    }

    @Override
    public UserVo login(UserDto userDto) {
        User user = userMapper.getByEmail(userDto.getEmail());
        System.out.println(userDto.getPassword());
        userDto.setPassword(StringUtil.encodeWithMd5(userDto.getPassword()));
        System.out.println(userDto.getPassword());
        if (user == null || !userDto.getPassword().equals(user.getPassword())) {
            throw  new CustomException("账号或密码错误");
        }

        checkStatus(user);
        //update last login time
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        UserVo resVo = new UserVo();
        BeanUtils.copyProperties(user, resVo);
        String token = userSecurity.createToken(resVo.getUid(), Constant.USER_IDENTITY_NORMAL);
        resVo.setToken(token);

        return resVo;
    }

    @Override
    public UserVo administratorLogin(UserDto userDto) {
        User user = userMapper.getByEmail(userDto.getEmail());
        if (user == null || userDto.getPassword().equals(user.getPassword())) {
            throw  new CustomException("账号或密码错误");
        }

        //check if the account is administrator
        String emails = runtimeConfig.get(Constant.ADMINISTRATOR_EMAIL);
        String[] emailList = emails.split("[,;]");
        if (!StringUtil.contains(emailList, userDto.getEmail())) {
            throw new CustomException("非管理员用户");
        }

        checkStatus(user);
        //update last login time
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        UserVo resVo = new UserVo();
        BeanUtils.copyProperties(user, resVo);
        String token = userSecurity.createToken(resVo.getUid(), Constant.USER_IDENTITY_ADMINISTRATOR);
        resVo.setToken(token);

        return resVo;
    }

    @Override
    public void resetPwd(UserDto userDto) {
        User user = userMapper.getByEmail(userDto.getEmail());
        if(user == null){
            throw new CustomException("用户不存在!!!");
        }

        userDto.setPassword(StringUtil.encodeWithMd5(userDto.getPassword()));
        userMapper.updatePasswordByEmail(userDto);
    }

    @Override
    public void resetUsername(ResetUserNameDto dto) {
        Long id = userSecurity.getId(dto.getToken());
        userMapper.updateUsernameById(id, dto.getName());
    }

    @Override
    public void getAvatar(Long id, HttpServletResponse response) {
        try {
            User user = userMapper.getById(id);
            File iconFile;

            if(user.getAvatarName() == null || user.getAvatarName().equals("")){
                iconFile = new File(pathUtil.getDefaultAvatarPath());
            }else {
                String iconPath = pathUtil.getAvatarFolderPath() + File.separator + user.getAvatarName();
                iconFile = FileUtil.getFileAndMkdirs(iconPath);
                if(!iconFile.exists()){
                    iconFile = new File(pathUtil.getDefaultAvatarPath());
                }
            }

            FileUtil.writeToResponse(iconFile, response);
        } catch (Exception e) {
            throw new CustomException("头像文件不存在");
        }
    }

    @Override
    public void uploadAvatar(MultipartFile file, Long id) {
        //原始文件名，截取后缀
        var originName = file.getOriginalFilename();
        var suffix = originName.substring(originName.lastIndexOf('.'));

        //使用uuid来生成文件名，防止文件名重复造成覆盖
        String randomId = IDUtil.getRandomId();
        String fileName = randomId + suffix;

        File avatarFile = FileUtil.getFileAndMkdirs(pathUtil.getAvatarFolderPath() + File.separator + fileName);

        //转存文件
        try {
            file.transferTo(avatarFile);
        } catch (IOException e) {
            throw new CustomException("转存文件失败");
        }
        //删除原头像文件
        User user = getById(id);
        File originFile = FileUtil.getFileAndMkdirs(pathUtil.getAvatarFolderPath() + File.separator + user.getAvatarName());
        if(!originFile.delete()){
            throw new CustomException("头像文件删除失败");
        }

        //将头像信息存储到mysql
        userMapper.updateAvatar(id, fileName);
    }

    @Override
    public void logOut(String token) {
        userSecurity.deleteToken(token);
    }

    public void checkStatus(User user){
        if (user.getStatus() == 0){
            throw new CustomException("账号已禁用");
        }
    }

    public void updateUserSpace(Long uid, Long userSpace){
        userMapper.updateUserSpaceById(uid, userSpace);
    }

    @Override
    public void updateUserSpaceByOffset(Long uid, Long offset) {
        userMapper.updateUserSpaceByIdAndOffset(uid, offset);
    }

    @Override
    public List<UserResVo> list(QueryDto dto) {
        if(dto.getOffset() == null) {
            dto.setOffset((dto.getPage() - 1) * dto.getPageSize());
        }
        var list = userMapper.list(dto);
        var res = new ArrayList<UserResVo>();
        for(var item:list){
            var temp = new UserResVo();
            BeanUtils.copyProperties(item,temp);
            res.add(temp);
        }
        return res;
    }

    @Override
    public void updateById(Long uid, String nickName, Integer status, Long totalSpace) {
        User user = getById(uid);
        if (nickName != null) {
            user.setNickName(nickName);
        }
        if (status != null) {
            user.setStatus(status);
        }
        if (totalSpace != null) {
            user.setTotalSpace(totalSpace);
        }

        userMapper.updateById(user);
    }

    @Override
    public Integer getSize() {
        return userMapper.getSize();
    }
}
