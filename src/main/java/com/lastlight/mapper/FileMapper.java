package com.lastlight.mapper;

import com.lastlight.entity.FileEntity;
import com.lastlight.entity.dto.FileQueryDto;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FileMapper {
    @Select("SELECT * FROM file WHERE file_parent = #{fileParent} AND user_id = #{userId} AND `status` = 5 LIMIT #{offset}, #{pageSize}")
    FileEntity[] getByUserIdAndParent(FileQueryDto fileQueryDto);

    @Select("SELECT * FROM file WHERE file_category = #{fileCategory} AND user_id = #{userId} AND `status` = 5 LIMIT #{offset}, #{pageSize}")
    FileEntity[] getByUserIdAndCategory(FileQueryDto fileQueryDto);

    @Select("SELECT * FROM file WHERE user_id = #{userId} AND file_parent = #{fileParent} AND folder_type = 1 AND `status` = 5 LIMIT #{offset}, #{pageSize}")
    FileEntity[] getDirByUserIdAndParent(FileQueryDto fileQueryDto);

    @Select("SELECT * FROM file WHERE file_parent = #{fileParent}")
    FileEntity[] getByFileParent(@Param("fileParent") String fileParent);

    @Select("SELECT count(*) FROM file WHERE name = #{name} AND file_parent = #{fileParent} AND `status` = 5")
    Integer countByFileNameAndParent(FileQueryDto fileQueryDto);

    @Select("SELECT * FROM file WHERE fid = #{fid}")
    FileEntity getByFid(@Param("fid") String fid);

    @Select("SELECT * FROM file WHERE file_md5 = #{fileMd5} AND `status` = 5 LIMIT #{offset}, #{pageSize}")
    FileEntity[] getByMd5(FileQueryDto fileQueryDto);

    @Insert("INSERT INTO `file` (`fid`, `name`, `file_path`, `user_id`, `file_md5`, `file_parent`, `file_size`, `file_cover`, `folder_type`, `file_category`, `status`, `create_time`, `last_update_time`) VALUES (#{fid}, #{name}, #{filePath}, #{userId}, #{fileMd5}, #{fileParent}, #{fileSize}, #{fileCover}, #{folderType}, #{fileCategory}, #{status}, #{createTime}, #{lastUpdateTime})")
    void add(FileEntity file);

    @Update("UPDATE file SET `name`= #{name} WHERE `fid`=#{fid} AND `status` = 5")
    void rename(@Param("name")String name, @Param("fid") String fid);

    @Update("UPDATE file SET `file_parent`= #{fileParent} WHERE `fid`=#{fid} AND `status` = 5")
    void move(@Param("fileParent")String fileParent, @Param("fid") String fid);

    @Update("UPDATE `file` SET `status`= #{status} WHERE `fid`=#{fid}")
    void changeStatus(@Param("status")Integer status, @Param("fid") String fid);

    @Delete("DELETE FROM `file` WHERE fid=#{fid}")
    void deleteByFid(@Param("fid")String fid);

    @Select("SELECT * FROM file WHERE file_parent = #{fileParent} AND `status` = 5 LIMIT #{offset}, #{pageSize}")
    FileEntity[] getByParent(FileQueryDto fileQueryDto);

    @Select("SELECT COUNT(*) FROM file WHERE file_parent = #{fileParent} AND file_category = #{fileCategory} AND user_id = #{uid} AND `status` = 5")
    Integer countByParentAndCategory(Long uid, String fileParent, int fileCategory);

    @Select("SELECT COUNT(*) FROM file WHERE file_parent = #{fileParent} AND user_id = #{uid} AND `status` = 5")
    Integer countByParent(Long uid, String fileParent);

    @Select("SELECT COUNT(*) FROM file WHERE file_path = #{filePath}")
    Integer countByFilePath(String filePath);

    @Select("SELECT * FROM file WHERE `status` = #{status} AND user_id = #{uid}")
    FileEntity[] listByStatusAndUID(Long uid,Integer status);
}
