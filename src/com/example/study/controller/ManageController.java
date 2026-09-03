package com.example.study.controller;

import com.example.study.common.Result;
import com.example.study.entity.ClassGrade;
import com.example.study.entity.Course;
import com.example.study.entity.User;
import com.example.study.mapper.ManageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manage")
public class ManageController {

    @Autowired
    private ManageMapper manageMapper;

    // 班级列表与创建
    @GetMapping("/classes")
    public Result<List<ClassGrade>> getClasses() {
        return Result.success(manageMapper.findAllClasses());
    }

    @PostMapping("/classes")
    public Result<String> createClass(@RequestBody ClassGrade classGrade) {
        manageMapper.insertClass(classGrade);
        return Result.success("班级创建成功！");
    }

    // 课程列表与创建
    @GetMapping("/courses")
    public Result<List<Course>> getCourses() {
        return Result.success(manageMapper.findAllCourses());
    }

    @PostMapping("/courses")
    public Result<String> createCourse(@RequestBody Course course) {
        manageMapper.insertCourse(course);
        return Result.success("课程开设成功！");
    }

    // 学生名单管理 (支持按班级筛选)
    @GetMapping("/students")
    public Result<List<User>> getStudents(@RequestParam(required = false) Long classId) {
        return Result.success(manageMapper.findStudentsByClass(classId));
    }
}