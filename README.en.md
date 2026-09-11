# Lab Management System

A modern lab management system based on Spring Boot + Vue 3, supporting functions such as student lab submission, teacher grading/scoring, and class/course management.

## Tech Stack

### Backend
- **Spring Boot 2.7.x** - Application Framework
- **MyBatis** - Database Persistence Layer
- **MySQL** - Relational Database
- **Redis** - Caching Support (Enabled via @EnableCaching)

### Frontend
- **Vue 3** - Frontend Framework
- **Vite** - Build Tool
- **Element Plus** - UI Component Library

## Project Structure

```
study/
├── src/main/java/com/example/study/
│   ├── Main.java                 # Application Entry Point
│   ├── common/
│   │   ├── CorsConfig.java       # CORS Configuration
│   │   └── Result.java           # Unified Response Wrapper
│   ├── controller/
│   │   ├── AuthController.java   # Authentication API (Login/Register)
│   │   ├── LabController.java    # Lab Management API
│   │   └── ManageController.java # Management API (Class/Course)
│   ├── entity/
│   │   ├── User.java             # User Entity
│   │   ├── LabTask.java          # Lab Task Entity
│   │   ├── LabSubmission.java    # Submission Record Entity
│   │   ├── Course.java           # Course Entity
│   │   └── ClassGrade.java       # Class Entity
│   └── mapper/
│       ├── UserMapper.java
│       ├── LabTaskMapper.java
│       ├── LabSubmissionMapper.java
│       └── ManageMapper.java
├── lab-web/                       # Vue Frontend Project
│   ├── src/
│   ├── public/
│   └── package.json
└── src/main/resources/
    ├── application.yml           # Configuration File
    └── static/                   # Built Static Assets
```

## Functional Modules

### 1. User Authentication
- User Login/Register
- Personal Information Management
- Password Change

### 2. Lab Management
- Lab Task Publication and Viewing
- File Upload Submission
- Draft Saving
- Submission Confirmation
- Grading and Feedback

### 3. Class & Course Management
- Class Creation and Management
- Course Management
- Student Information Management

### 4. Data Export
- Support exporting student submission records by lab (ZIP format)

## Quick Start

### Environment Requirements
- JDK 8+
- MySQL 5.7+ / 8.0+
- Node.js 16+
- Redis (Optional)

### Backend Configuration

1. Create Database:
```sql
CREATE DATABASE study DEFAULT CHARACTER SET utf8mb4;
```

2. Modify `src/main/resources/application.yml` to configure database connection:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/study
    username: your_username
    password: your_password
```

3. Run Application:
```bash
mvn spring-boot:run
```

The backend service will start at http://localhost:8080

### Frontend Development

```bash
cd lab-web
npm install
npm run dev
```

The frontend development server will start at http://localhost:3000

### Production Deployment

Frontend Build:
```bash
cd lab-web
npm run build
```

Built static files will be automatically copied to the `src/main/resources/static/` directory and deployed along with the Spring Boot application.

## API Endpoints

### Authentication API
| Endpoint | Method | Description |
|------|------|------|
| `/api/auth/login` | POST | User Login |
| `/api/auth/register` | POST | User Registration |
| `/api/auth/profile` | POST | Update Personal Information |
| `/api/auth/change-password` | POST | Change Password |

### Lab Management API
| Endpoint | Method | Description |
|------|------|------|
| `/api/lab/tasks` | GET | Get Lab Task List |
| `/api/lab/tasks` | POST | Create Lab Task |
| `/api/lab/upload` | POST | File Upload |
| `/api/lab/draft` | POST | Save Draft |
| `/api/lab/confirm-submit` | POST | Confirm Submission |
| `/api/lab/grade` | POST | Grade Submission |
| `/api/lab/submissions/task/{taskId}/export-zip` | GET | Export Submission Records |

### Management API
| Endpoint | Method | Description |
|------|------|------|
| `/api/manage/classes` | GET/POST | Class Management |
| `/api/manage/courses` | GET/POST | Course Management |
| `/api/manage/students` | GET | Get Student List |

## User Roles

The system supports two user roles:
- **Teacher**: Publishes lab tasks, grades assignments, manages classes and courses.
- **Student**: Views tasks, submits assignments, views grades.

## License

This project is for learning and research purposes only.