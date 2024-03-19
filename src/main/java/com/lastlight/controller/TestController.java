package com.lastlight.controller;

import com.lastlight.annotation.GlobalInterceptor;
import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.annotation.VerifyObjectParam;
import com.lastlight.common.RegularConstant;
import com.lastlight.entity.User;
import com.lastlight.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private UserService userService;
    @GetMapping
    @GlobalInterceptor(checkParams = true)
    public String test(@VerifyObjectParam User user){
        return "OK";
    }
}
