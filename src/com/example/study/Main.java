package com.example.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.study.mapper")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("===============================================");
        System.out.println("  学生实验报告系统后端服务启动成功! 端口: 8080  ");
        System.out.println("===============================================");
    }
}