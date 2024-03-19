package com.lastlight.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
//automatic set the value with content of the spring default yaml file.
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class EmailConfig {
    private String userName;
    private String password;
    public static final String SUBJECT_KEY = "mail-subject";
    public static final String CONTENT_KEY = "mail-content";
}
