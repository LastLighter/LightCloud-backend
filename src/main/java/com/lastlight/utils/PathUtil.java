package com.lastlight.utils;

import com.lastlight.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Component
public class PathUtil {
    @Autowired
    private AppConfig appConfig;

    public static String getWorkPath(){
        return System.getProperty("user.dir");
    }

    public String getAvatarFolderPath(){
        return getWorkPath() + File.separatorChar + appConfig.getAvatarFolderName();
    }

    public String getDefaultAvatarPath(){
        return getWorkPath() + File.separatorChar + appConfig.getAvatarFolderName() + File.separatorChar + appConfig.getDefaultAvatarName();
    }

    public String getUserFileFolderBasePath(){
        return getWorkPath() + File.separatorChar + appConfig.getUserFileFolderName();
    }

    public String getUserFileFolderPath(int year, int month, int day){
        return getUserFileFolderBasePath() + File.separatorChar + year + File.separatorChar + month + File.separatorChar + day;
    }

    public String getUserFileFolderPath(LocalDateTime localDateTime){
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        return getUserFileFolderPath(year, month, day);
    }

    public String getTempFolderPath(){
        return getWorkPath() + File.separatorChar + appConfig.getTempFolderName();
    }

    public String getFileMetaDatePath(String fileRelativePath){
        return getUserFileFolderBasePath() + FileUtil.getNoSuffixPath(fileRelativePath);
    }

    public static String getLastPath(String path, int index){
        int count = 0;
        for(int i = path.length() -1; i > 0; --i){
            if(path.charAt(i) == File.separatorChar){
                ++count;
                if(count == index){
                    return path.substring(i + 1);
                }
            }
        }
        throw new RuntimeException("路径少于指定层级");
    }
}
