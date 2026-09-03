package com.example.study.mapper;

import com.example.study.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT u.*, c.class_name as className FROM sys_user u " +
            "LEFT JOIN class_grade c ON u.class_id = c.id " +
            "WHERE u.username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Select("SELECT u.*, c.class_name as className FROM sys_user u " +
            "LEFT JOIN class_grade c ON u.class_id = c.id " +
            "WHERE u.id = #{id} LIMIT 1")
    User findById(@Param("id") Long id);

    // 用户注册
    @Insert("INSERT INTO sys_user(username, password, real_name, role, class_id, email, phone) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{classId}, #{email}, #{phone})")
    int insert(User user);

    // 修改个人资料
    @Update("UPDATE sys_user SET real_name = #{realName}, email = #{email}, phone = #{phone} WHERE id = #{id}")
    int updateProfile(User user);

    // 修改密码
    @Update("UPDATE sys_user SET password = #{newPassword} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);
}