package com.lastlight.utils;

import com.lastlight.common.Constant;
import com.lastlight.config.EmailConfig;
import com.lastlight.config.RuntimeConfig;
import com.lastlight.entity.dto.SysSettingDto;

public class ConfigUtil {
    public static void setSysSetting(RuntimeConfig runtimeConfig, SysSettingDto settingDto){
        runtimeConfig.set(EmailConfig.SUBJECT_KEY, settingDto.getEmailSubject());
        runtimeConfig.set(EmailConfig.CONTENT_KEY, settingDto.getEmailContent());
        runtimeConfig.set(Constant.BASE_SPACE, settingDto.getBaseSpace());
    }

    public static SysSettingDto getSysSetting(RuntimeConfig runtimeConfig){
        SysSettingDto dto = new SysSettingDto();
        dto.setEmailSubject(runtimeConfig.get(EmailConfig.SUBJECT_KEY));
        dto.setEmailContent(runtimeConfig.get(EmailConfig.CONTENT_KEY));
        dto.setBaseSpace(runtimeConfig.get(Constant.BASE_SPACE));
        return dto;
    }
}
