package com.lastlight.security;

import com.lastlight.common.Constant;
import com.lastlight.entity.User;
import com.lastlight.exception.CustomException;
import com.lastlight.exception.TokenException;
import com.lastlight.service.UserService;
import com.lastlight.utils.IDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class UserSecurity {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    //period of validity
    public static final Integer TokenPeriod = 24;

    public void checkToken(String token, Long id, Integer minutes, String userIdentity){
        String[] substrings = get(token);
        String uid = substrings[0];
        String dateTime = substrings[1];
        String identity = substrings[2];

        if (id != null) {
            if( uid == null || uid != String.valueOf(id)){
                throw new CustomException("无对指定用户的操作权限");
            }
        }

        if(minutes != null){
            LocalDateTime loginTime = LocalDateTime.parse(dateTime);
            LocalDateTime now = LocalDateTime.now();
            Duration between = Duration.between(loginTime, now);
            long seconds = between.getSeconds();

            if(minutes < seconds / 60){
                throw new TokenException();
            }
        }

        if(userIdentity != null){
            if(!userIdentity.equals(identity) && userIdentity.equals(Constant.USER_IDENTITY_ADMINISTRATOR)){
                throw new CustomException("无权限操作");
            }
        }
    }

    public Long getId(String token){
        return Long.valueOf(get(token)[0]);
    }

    public String[] get(String token){
        String value = (String) redisTemplate.opsForValue().get(token);
        if (value == null) {
            throw new TokenException("登录信息已过期，请重新登录");
        }
        String[] substrings = value.split(",");
        return substrings;
    }

    public String createToken(Long id, String userIdentity){
        //create unique token
        String token = IDUtil.getUUID();

        LocalDateTime dateTime = LocalDateTime.now();
        String value = String.valueOf(id) + ',' + dateTime.toString() + ',' + userIdentity;
        //stores in redis
        redisTemplate.opsForValue().set(token,value, TokenPeriod, TimeUnit.HOURS);
        return token;
    }

    public void deleteToken(String token){
        redisTemplate.delete(token);
    }
}
