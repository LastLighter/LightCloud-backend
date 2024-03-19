package com.lastlight;

import com.lastlight.config.AppConfig;
import com.lastlight.config.EmailConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//定时器用到
@EnableScheduling
//异步转码要用到
@EnableAsync
@EnableTransactionManagement
//允许配置文件属性注入
@EnableConfigurationProperties({AppConfig.class, EmailConfig.class})
@SpringBootApplication
public class LightCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(LightCloudApplication.class, args);
    }

}
