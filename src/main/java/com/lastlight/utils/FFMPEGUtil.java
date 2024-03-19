package com.lastlight.utils;

import com.lastlight.common.Constant;
import com.lastlight.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FFMPEGUtil {
    @Autowired
    private AppConfig appConfig;

    //截取的是第一帧
    private static final String ScreenShot = "ffmpeg -y -i %s -frames:v 1 -vf scale=%d:-1 %s";
    private static final String ThumbNail = "ffmpeg -y -i %s -vf scale=%d:-1 %s";
    //flac格式的音频ts本身不支持，所以会没声音，我们干脆强转aac编码。
    private static final String CutTs = "ffmpeg -i %s -c:v copy -c:a aac -map 0 -f segment -segment_list %s -segment_time %d %s/%4d.ts";
    public void cutVideoToTs(String filePath){
        //创建同名文件夹
        String tsFolder = filePath;
        FileUtil.getFileAndMkdirs(tsFolder);
        String cmd = String.format(CutTs, filePath, Constant.M3U8_FILE_NAME, appConfig.getSegmentLength(), tsFolder);

        ProcessUtil.exec(cmd, false);
    }

    public void screenShot(String filePath){
        String tsFolder = FileUtil.getNoSuffixPath(filePath);
        FileUtil.getFileAndMkdirs(tsFolder);
        String imagePath = tsFolder + File.separatorChar + Constant.SCREEN_SHOT_FILE_NAME;
        String cmd = String.format(ScreenShot, filePath, appConfig.getScreenShotWidth(), imagePath);

        ProcessUtil.exec(cmd, false);
    }

    public void thumbnail(String imagePath){
        String thumbImage = imagePath.replace(".", "_.");
        String cmd = String.format(ThumbNail, imagePath, appConfig.getScreenShotWidth(), thumbImage);

        ProcessUtil.exec(cmd, false);
    }
}
