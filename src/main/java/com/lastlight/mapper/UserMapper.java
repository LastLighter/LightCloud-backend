package com.lastlight.mapper;

import com.lastlight.entity.User;
import com.lastlight.entity.dto.QueryDto;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE `uid` = #{uid} FOR UPDATE")
    User getById(@Param("uid")Long id);

    @Insert("INSERT INTO user(`uid`,`password`) VALUES (#{uid},#{password})")
    void add(@Param("uid") String id, @Param("password") String password);

    @Insert("INSERT INTO user(`uid`,`nick_name`,`password`,`email`,`status`,`user_space`,`total_space`,`create_time`) VALUES (#{uid},#{nickName},#{password},#{email},#{status},#{userSpace},#{totalSpace},#{createTime})")
    void register(User user);

    @Select("SELECT * FROM user WHERE `email` = #{email}")
    User getByEmail(@Param("email")String email);

    @Select("SELECT count(*) FROM user")
    Integer getSize();

    @Update("UPDATE user SET `nick_name`=#{nickName}, `avatar_name`=#{avatarName}, `last_login_time`=#{lastLoginTime}, `status`=#{status}, `total_space`= #{totalSpace} WHERE `uid`=#{uid}")
    void updateById(User user);

    @Update("UPDATE user SET `user_space`= #{userSpace} WHERE `uid`=#{uid}")
    void updateUserSpaceById(@Param("uid") Long id, @Param("userSpace") Long userSpace);

    @Update("UPDATE user SET `user_space`= user_space + #{offset} WHERE `uid`=#{uid}")
    void updateUserSpaceByIdAndOffset(@Param("uid") Long id, Long offset);

    @Update("UPDATE user SET `nick_name`= #{username} WHERE `uid`=#{uid}")
    void updateUsernameById(@Param("uid") Long id, String username);

    @Update("UPDATE user SET `password`= #{password} WHERE `email`=#{email}")
    void updatePasswordByEmail(User user);

    @Update("UPDATE user SET `avatar_name`= #{iconName} WHERE `uid`=#{uid}")
    void updateAvatar(@Param("uid") Long id, @Param("iconName") String iconName);

    @Delete("DELETE user WHERE `uid`=#{uid}")
    void delete(@Param("uid")String uid);

    @Select("SELECT * FROM `user` LIMIT #{offset}, #{pageSize}")
    User[] list(QueryDto queryDto);
}
