package com.lastlight.utils;

import com.lastlight.config.AppConfig;
import com.lastlight.config.EmailConfig;
import com.lastlight.config.RuntimeConfig;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.Properties;

@Component
@Slf4j
public class EmailUtil {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private RuntimeConfig runtimeConfig;



    @Async
    public void sendCheckCode(String toEmail,String code){
        runtimeConfig.loadConfig();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getUserName());
            helper.setTo(toEmail);
            helper.setSubject(runtimeConfig.get(EmailConfig.SUBJECT_KEY));
            //create string with template
            String text = String.format(runtimeConfig.get(EmailConfig.CONTENT_KEY), code);
            helper.setText(text);
            helper.setSentDate(new Date());

            javaMailSender.send(message);
        }catch (Exception e){
            log.error(e.getMessage());
            log.error("邮件发送错误");
        }
    }
}
