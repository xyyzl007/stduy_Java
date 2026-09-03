package com.example.study.mapper;

import com.example.study.entity.ClassGrade;
import com.example.study.entity.Course;
import com.example.study.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ManageMapper {

    // 班级管理
    @Select("SELECT * FROM class_grade ORDER BY id DESC")
    List<ClassGrade> findAllClasses();

    @Insert("INSERT INTO class_grade(class_name, grade_year) VALUES(#{className}, #{gradeYear})")
    int insertClass(ClassGrade classGrade);

    // 课程管理
    @Select("SELECT c.*, u.real_name as teacherName FROM course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id ORDER BY c.id DESC")
    List<Course> findAllCourses();

    @Insert("INSERT INTO course(course_code, course_name, teacher_id, semester, description) " +
            "VALUES(#{courseCode}, #{courseName}, #{teacherId}, #{semester}, #{description})")
    int insertCourse(Course course);

    // 学生名单管理 (可按班级查看)
    @Select("<script>" +
            "SELECT u.*, c.class_name as className FROM sys_user u " +
            "LEFT JOIN class_grade c ON u.class_id = c.id " +
            "WHERE u.role = 'ROLE_STUDENT' " +
            "<if test='classId != null'> AND u.class_id = #{classId} </if> " +
            "ORDER BY u.id ASC" +
            "</script>")
    List<User> findStudentsByClass(Long classId);
}