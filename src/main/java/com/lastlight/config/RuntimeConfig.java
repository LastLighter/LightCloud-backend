package com.lastlight.config;

import com.lastlight.exception.CustomException;
import com.lastlight.utils.FileUtil;
import com.lastlight.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class RuntimeConfig {
    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private AppConfig appConfig;
    private File configFile;
    private Properties configProperties = new Properties();
    //if the content of source file has changed
    private boolean modified = false;

    public void loadConfig(){
        if(configFile == null)
            findConfigPath();
        try {
            //if the file does not exist, then create it
            if (!configFile.exists()) {
                configFile.createNewFile();

                setDefault();
                saveConfig();
            }else {
                Reader reader = new FileReader(configFile);
                //load config
                configProperties.load(reader);
            }
        }catch (Exception e){
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveConfig(){
        if(configFile == null)
            findConfigPath();
        try {
            Writer writer = new FileWriter(configFile);
            configProperties.store(writer,"update");
        }catch (Exception e){
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public File findConfigPath(){
        //get the config file path
        String resourcePath = PathUtil.getWorkPath() + File.separator + appConfig.getConfigFileName();

        configFile = new File(resourcePath);
        return configFile;
    }

    private void setDefault(){
        //set the default config
        for(Map.Entry entry: appConfig.getDefaultConfig().entrySet()){
            try {
                configProperties.setProperty((String) entry.getKey(), (String) entry.getValue());
            } catch (Exception e){
                log.error(e.getMessage());
                throw new CustomException("设置默认配置错误");
            }
        }
    }

    public void reset(){
        setDefault();
        saveConfig();
    }

    public String get(String key){
        if(configProperties.isEmpty() || modified){
            loadConfig();
        }
        return (String) configProperties.get(key);
    }

    public void set(String key, String value){
        if(configProperties.isEmpty()){
            loadConfig();
        }
        configProperties.setProperty(key, value);
        saveConfig();
    }
}
