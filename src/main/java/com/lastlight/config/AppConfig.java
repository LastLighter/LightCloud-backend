package com.lastlight.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Configuration
//automatic set the value with content of the spring default yaml file.
@ConfigurationProperties(prefix = "light-cloud.app")
@Data
public class AppConfig {
    //the config file name
    private String configFileName;
    private String avatarFolderName;
    private String avatarPrefix;
    private String defaultAvatarName;
    private Integer tokenExpiredTime;
    private String userFileFolderName;
    private String tempFolderName;
    //视频切片的长度
    private Integer segmentLength;
    //视频封面的缩略图宽度
    private Integer screenShotWidth;
    private Map<String, String> defaultConfig;
}
