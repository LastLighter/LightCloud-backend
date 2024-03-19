package com.lastlight.utils;

import com.lastlight.common.FileConstant;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    private static final String[] VideoSuffix = {"mp4","m4v","mkv","avi","mov"};
    private static final String[] MusicSuffix = {"mp3","flac","m4a","wav","aac"};
    private static final String[] ImageSuffix = {"png","jpg","jpeg","webp","gif","svg"};
    private static final String[] DocumentSuffix = {"pdf","doc","docx","xlsx","xls","md","txt"};
    private static final String[] ArchiveSuffix = {"zip","7z","rar","tar","gz"};
    public static List<String> videos = Arrays.asList(VideoSuffix);
    public static List<String> musics = Arrays.asList(MusicSuffix);
    public static List<String> images = Arrays.asList(ImageSuffix);
    public static List<String> archives = Arrays.asList(ArchiveSuffix);
    public static List<String> documents = Arrays.asList(DocumentSuffix);

    public static File getFileAndMkdirs(String path){
        //判断存放目录是否存在（如果不存在会发生异常）
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void writeToResponse(File resource, HttpServletResponse response) throws Exception{
        InputStream ins = new FileInputStream(resource);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(ins);

        ServletOutputStream ous = response.getOutputStream();

        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = bufferedInputStream.read(bytes)) != -1){
            ous.write(bytes,0,len);
        }
        //缓冲区满才会写入，最后不满的时候，我们就需要手动flush
        ous.flush();
        bufferedInputStream.close();
        ous.close();
    }

    public static void sliceWriteToResponse(HttpServletResponse response, Long start, Long end, File resource, int chunkSize) throws Exception{
        byte[] buffer = new byte[chunkSize];
        // 分块下载
        int bufferSize = 1024 * 1024; // 每次读取的缓冲区大小为 1MB
        long contentLength = end - start;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(resource, "r");
             OutputStream outputStream = response.getOutputStream()) {
            randomAccessFile.seek(start);

            long remaining = contentLength;
            while (remaining > 0) {
                int bytesRead = randomAccessFile.read(buffer, 0, (int) Math.min(bufferSize, remaining));
                if (bytesRead < 0) {
                    break;
                }
                outputStream.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            outputStream.flush();
        } catch (Exception e) {
            // 异常处理
        }
    }

    public static Integer getCategory(String suffix){
        if (videos.contains(suffix)) {
            return FileConstant.FILE_CATEGORY_VIDEO;
        }

        if (musics.contains(suffix)) {
            return FileConstant.FILE_CATEGORY_MUSIC;
        }

        if (images.contains(suffix)) {
            return FileConstant.FILE_CATEGORY_IMAGE;
        }

        if (archives.contains(suffix)) {
            return FileConstant.FILE_CATEGORY_ARCHIVE;
        }

        if (documents.contains(suffix)) {
            return FileConstant.FILE_CATEGORY_DOCUMENT;
        }
        return FileConstant.FILE_CATEGORY_OTHER;
    }

    public static void merge(File[] sources, String targetPath) {
        OutputStream ous = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            byte[] array = new byte[1024];

            File target = new File(targetPath);
            //如果父级路径不存在，无法写入文件
            getFileAndMkdirs(target.getParent());
            ous = new FileOutputStream(target);
            bufferedOutputStream = new BufferedOutputStream(ous);
            for (File file : sources) {
                InputStream ins = new FileInputStream(file);
                BufferedInputStream bufferedIns = new BufferedInputStream(ins);
                int len = 0;
                while ((len = bufferedIns.read(array)) != -1) {
                    bufferedOutputStream.write(array, 0, len);
                }
                bufferedOutputStream.flush();
                ins.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                ous.close();
                bufferedOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static String getSuffix(String fileName){
        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static String getNoSuffixPath(String filePath){
        return filePath.substring(0, filePath.indexOf("."));
    }

    public static String getDirPath(String filePath){
        return  filePath.substring(0, filePath.lastIndexOf(File.separatorChar));
    }
}
