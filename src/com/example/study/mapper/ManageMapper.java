package com.example.study.mapper;

import com.example.study.entity.ClassGrade;
import com.example.study.entity.Course;
import com.example.study.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ManageMapper {

    // 班级管理
    @Select("SELECT * FROM class_grade ORDER BY id DESC")
    List<ClassGrade> findAllClasses();

    @Insert("INSERT INTO class_grade(class_name, grade_year) VALUES(#{className}, #{gradeYear})")
    int insertClass(ClassGrade classGrade);

    @Update("UPDATE sys_user SET class_id = NULL WHERE class_id = #{id}")
    int clearStudentClassId(Long id);

    @Delete("DELETE FROM class_grade WHERE id = #{id}")
    int deleteClass(Long id);

    // 课程管理
    @Select("SELECT c.*, u.real_name as teacherName FROM course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id ORDER BY c.id DESC")
    List<Course> findAllCourses();

    @Insert("INSERT INTO course(course_code, course_name, teacher_id, semester, description) " +
            "VALUES(#{courseCode}, #{courseName}, #{teacherId}, #{semester}, #{description})")
    int insertCourse(Course course);

    @Delete("DELETE FROM lab_submission WHERE task_id IN (SELECT id FROM lab_task WHERE course_id = #{courseId})")
    int deleteSubmissionsByCourseId(Long courseId);

    @Delete("DELETE FROM lab_task WHERE course_id = #{courseId}")
    int deleteTasksByCourseId(Long courseId);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteCourse(Long id);

    // 【彻底修复】：健壮的模糊搜索 + 未分配班级精准匹配 (免XML标签，零解析错误)
    @Select("SELECT u.*, c.class_name as className FROM sys_user u " +
            "LEFT JOIN class_grade c ON u.class_id = c.id " +
            "WHERE u.role = 'ROLE_STUDENT' " +
            "AND (#{classId} IS NULL OR #{classId} = 0 " +
            "     OR (#{classId} = -1 AND (u.class_id IS NULL OR c.id IS NULL)) " +
            "     OR (#{classId} > 0 AND u.class_id = #{classId})) " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' " +
            "     OR u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR u.real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY u.id ASC")
    List<User> findStudents(@Param("classId") Long classId, @Param("keyword") String keyword);

    // 老师为学生重新分配班级
    @Update("UPDATE sys_user SET class_id = #{classId} WHERE id = #{studentId}")
    int updateStudentClass(@Param("studentId") Long studentId, @Param("classId") Long classId);
}