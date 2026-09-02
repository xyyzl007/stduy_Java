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

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }
        user.setPassword(null); // 不回传密码
        return Result.success(user);
    }
}