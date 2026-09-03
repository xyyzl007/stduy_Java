package com.example.study.controller;

import com.example.study.common.Result;
import com.example.study.entity.User;
import com.example.study.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    // 1. 登录
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Result.error("账号或密码错误");
        }
        user.setPassword(null); // 安全处理，脱敏
        return Result.success(user);
    }

    // 2. 注册 (学生/教师)
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return Result.error("该学号/工号已被注册！");
        }
        userMapper.insert(user);
        return Result.success("账号注册成功，请直接登录！");
    }

    // 3. 编辑个人资料
    @PostMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        userMapper.updateProfile(user);
        User updated = userMapper.findById(user.getId());
        updated.setPassword(null);
        return Result.success(updated);
    }

    // 4. 修改密码
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("newPassword");

        User user = userMapper.findById(userId);
        if (user == null || !user.getPassword().equals(oldPassword)) {
            return Result.error("原密码输入不正确！");
        }
        userMapper.updatePassword(userId, newPassword);
        return Result.success("密码修改成功，请谨记新密码！");
    }
}