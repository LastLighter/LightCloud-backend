package com.lastlight.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ProcessUtil {
    public static void exec(String command, boolean logOutput){
        try {
            Process process = Runtime.getRuntime().exec(command);
            if(logOutput) {
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String msg;
                while ((msg = bufferedReader.readLine()) != null) {
                    log.info(msg);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.error("命令行执行出错");
            throw new RuntimeException(e);
        }
    }
}
