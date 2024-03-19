package com.lastlight.mapper;

import com.lastlight.entity.ShareEntity;
import com.lastlight.entity.dto.ShareQueryDto;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ShareMapper {
    @Select("select * from share LIMIT #{offset}, #{pageSize}")
    ShareEntity[] listAll(ShareQueryDto queryDto);

    @Select("select * from share WHERE uid = #{uid} LIMIT #{offset}, #{pageSize}")
    ShareEntity[] list(ShareQueryDto queryDto);

    @Insert("INSERT INTO `share` VALUES (#{shareId}, #{fid}, #{uid}, #{validity}, #{createTime}, #{expiredTime}, #{code}, #{view})")
    void save(ShareEntity shareEntity);

    @Delete("DELETE FROM `share` WHERE share_id=#{shareId} AND uid = #{uid}")
    void deleteByShareId(@Param("shareId")String shareId, @Param("uid")Long uid);

    @Select("SELECT * FROM `share` WHERE code = #{code} AND share_id = #{shareId}")
    ShareEntity getByCode(@Param("shareId") String shareId, @Param("code")String code);

    @Update("UPDATE `share` SET view = #{view} WHERE share_id = #{shareId}")
    void updateView(@Param("view")Integer view, String shareId);

    @Select("SELECT * FROM `share` WHERE share_id = #{shareId}")
    ShareEntity get(@Param("shareId") String shareId);


}
