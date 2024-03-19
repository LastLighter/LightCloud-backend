package com.lastlight;

import com.lastlight.config.AppConfig;
import com.lastlight.config.EmailConfig;
import com.lastlight.config.RuntimeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LightCloudApplicationTests {
    @Autowired
    RuntimeConfig runtimeConfig;

    @Test
    void contextLoads() {
        runtimeConfig.loadConfig();
    }

}
