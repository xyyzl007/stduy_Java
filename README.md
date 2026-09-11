

# 实验室管理系统 (Lab Management System)

一个基于 Spring Boot + Vue 3 的现代化实验室管理系统，支持学生实验提交、教师批改打分、班级课程管理等功能。

## 技术栈

### 后端
- **Spring Boot 2.7.x** - 应用框架
- **MyBatis** - 数据库持久层
- **MySQL** - 关系型数据库
- **Redis** - 缓存支持 (通过 @EnableCaching 启用)

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库

## 项目结构

```
study/
├── src/main/java/com/example/study/
│   ├── Main.java                 # 应用启动类
│   ├── common/
│   │   ├── CorsConfig.java       # 跨域配置
│   │   └── Result.java           # 统一响应结果封装
│   ├── controller/
│   │   ├── AuthController.java   # 认证接口 (登录/注册)
│   │   ├── LabController.java    # 实验管理接口
│   │   └── ManageController.java # 管理接口 (班级/课程)
│   ├── entity/
│   │   ├── User.java             # 用户实体
│   │   ├── LabTask.java          # 实验任务实体
│   │   ├── LabSubmission.java    # 提交记录实体
│   │   ├── Course.java           # 课程实体
│   │   └── ClassGrade.java       # 班级实体
│   └── mapper/
│       ├── UserMapper.java
│       ├── LabTaskMapper.java
│       ├── LabSubmissionMapper.java
│       └── ManageMapper.java
├── lab-web/                       # Vue 前端项目
│   ├── src/
│   ├── public/
│   └── package.json
└── src/main/resources/
    ├── application.yml           # 配置文件
    └── static/                   # 构建后的静态资源
```

## 功能模块

### 1. 用户认证
- 用户登录/注册
- 个人信息管理
- 密码修改

### 2. 实验管理
- 实验任务发布与查看
- 文件上传提交
- 草稿保存
- 提交确认
- 成绩评定与反馈

### 3. 班级课程管理
- 班级创建与管理
- 课程管理
- 学生信息管理

### 4. 数据导出
- 支持按实验导出学生提交记录 (ZIP格式)

## 快速开始

### 环境要求
- JDK 8+
- MySQL 5.7+ / 8.0+
- Node.js 16+
- Redis (可选)

### 后端配置

1. 创建数据库:
```sql
CREATE DATABASE study DEFAULT CHARACTER SET utf8mb4;
```

2. 修改 `src/main/resources/application.yml` 配置数据库连接:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/study
    username: your_username
    password: your_password
```

3. 运行应用:
```bash
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端开发

```bash
cd lab-web
npm install
npm run dev
```

前端开发服务器将在 http://localhost:3000 启动

### 生产部署

前端构建:
```bash
cd lab-web
npm run build
```

构建后的静态文件会自动复制到 `src/main/resources/static/` 目录，随 Spring Boot 应用一起部署。

## API 接口

### 认证接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/profile` | POST | 更新个人信息 |
| `/api/auth/change-password` | POST | 修改密码 |

### 实验管理接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/lab/tasks` | GET | 获取实验任务列表 |
| `/api/lab/tasks` | POST | 创建实验任务 |
| `/api/lab/upload` | POST | 文件上传 |
| `/api/lab/draft` | POST | 保存草稿 |
| `/api/lab/confirm-submit` | POST | 确认提交 |
| `/api/lab/grade` | POST | 评分 |
| `/api/lab/submissions/task/{taskId}/export-zip` | GET | 导出提交记录 |

### 管理接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/manage/classes` | GET/POST | 班级管理 |
| `/api/manage/courses` | GET/POST | 课程管理 |
| `/api/manage/students` | GET | 获取学生列表 |

## 用户角色

系统支持两种用户角色：
- **教师 (teacher)**: 发布实验任务、批改作业、管理班级课程
- **学生 (student)**: 查看任务、提交作业、查看成绩

## 许可证

本项目仅供学习研究使用。