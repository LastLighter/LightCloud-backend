package com.lastlight.utils;

import com.lastlight.common.RedisConstant;
import com.lastlight.exception.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TempUserFileUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private PathUtil pathUtil;
    private String basePath;
    private static final Integer redisTimeout = 30;

    @PostConstruct
    public void init() {
        basePath = pathUtil.getTempFolderPath();
    }

    public Long getTempSpace(String fid){
        Long res = 0L;
        Object space = redisTemplate.opsForValue().get(RedisConstant.TEMP_FILE_PREFIX + fid);
        if(space != null && space instanceof Number){
            res = Long.valueOf(((Number) space).longValue());
        }
        return res;
    }

    public void setTempSpace(String fid, Long space){
        redisTemplate.opsForValue().set(RedisConstant.TEMP_FILE_PREFIX + fid, space, redisTimeout, TimeUnit.MINUTES);
    }

    public void write(MultipartFile file, String fid, Integer chunkIndex){
        try {
            String path = basePath + File.separatorChar + fid + '-' + chunkIndex;
            setTempSpace(fid,getTempSpace(fid) + file.getSize());
            File f = new File(path);
            file.transferTo(f);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteFile(String fid){
        File base = new File(basePath);
        FilenameFilter fidFilter = (dir, name) -> name.startsWith(fid);
        File[] files = base.listFiles(fidFilter);
        for(File file:files){
            if(!file.delete()){
                throw new CustomException("temp文件删除失败");
            }
        }

        setTempSpace(fid,0L);
    }
}
