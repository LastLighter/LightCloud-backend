package com.lastlight.controller;

import com.lastlight.annotation.GlobalInterceptor;
import com.lastlight.annotation.VerifyBaseParam;
import com.lastlight.common.RedisConstant;
import com.lastlight.common.RegularConstant;
import com.lastlight.controller.res.Result;
import com.lastlight.entity.dto.CheckCode;
import com.lastlight.utils.CheckCodeUtil;
import com.lastlight.utils.EmailUtil;
import com.lastlight.utils.IDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/code")
public class CheckCodeController {
    @Autowired
    private CheckCodeUtil codeUtil;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private EmailUtil emailUtil;

    /**
     * o create code image for user Login and register(with email)
     * @return
     */
    @RequestMapping("/iconCheckCode")
    @GetMapping
    public Result<String> iconCheckCode(){
        BufferedImage image = codeUtil.createImage();
        String code = codeUtil.getText();

        //store in redis,a code will bind with an uuid
        String key = RedisConstant.CHECK_CODE_USER + IDUtil.getUUID();
        try {
            //set automatic expiration time to 10 min
            redisTemplate.opsForValue().set(key, code, 10, TimeUnit.MINUTES);
        }catch (Exception e){
            log.error("无法连接redis");
        }

        //translate the BufferedImage into Base64 with jpg coded
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            ImageIO.write(image,"jpg",byteArrayOutputStream);
        }catch (Exception e){
            e.printStackTrace();
            log.error("图片输出有误");
        }
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return Result.success(new CheckCode(key,base64Image));
    }
    @RequestMapping("/emailCheckCode")
    @GetMapping
    @GlobalInterceptor
    public Result<String> emailCheckCode(@VerifyBaseParam(required = true, regEx = RegularConstant.REG_EMAIL, max = 150) String email){
        codeUtil.createCode(6);
        String code = codeUtil.getText();

        String codeId = IDUtil.getUUID();
        //store in redis,a code will bind with an uuid
        String key = RedisConstant.CHECK_CODE_EMAIL + codeId;
        //set automatic expiration time to 10 min
        redisTemplate.opsForValue().set(key,code,10, TimeUnit.MINUTES);

        //send email
        emailUtil.sendCheckCode(email, code);

        return Result.success(key);
    }
}
