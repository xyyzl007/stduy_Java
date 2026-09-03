package com.example.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.example.study.mapper")
public class Main extends SpringBootServletInitializer {

    // 继承 SpringBootServletInitializer，支持外置 Tomcat 部署 War 包
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Main.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("===============================================");
        System.out.println("  学生实验报告系统后端服务启动成功! 端口: 8080  ");
        System.out.println("===============================================");
    }
}