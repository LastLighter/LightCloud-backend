package com.lastlight.service;

import com.lastlight.entity.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRegister(){
        UserDto dto = new UserDto();
        dto.setCode("9527");
        dto.setCodeKey("user12");
        dto.setNickName("lastlight");
        dto.setPassword("7758258");
        dto.setEmail("274651245");

        userService.register(dto);
    }
}
