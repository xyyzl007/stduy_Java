package com.example.study.controller;

import com.example.study.common.Result;
import com.example.study.entity.ClassGrade;
import com.example.study.entity.Course;
import com.example.study.entity.User;
import com.example.study.mapper.LabTaskMapper;
import com.example.study.mapper.ManageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manage")
public class ManageController {

    @Autowired
    private ManageMapper manageMapper;

    @Autowired
    private LabTaskMapper taskMapper;

    // 班级列表
    @GetMapping("/classes")
    public Result<List<ClassGrade>> getClasses() {
        return Result.success(manageMapper.findAllClasses());
    }

    // 新建班级
    @PostMapping("/classes")
    public Result<String> createClass(@RequestBody ClassGrade classGrade) {
        manageMapper.insertClass(classGrade);
        return Result.success("班级创建成功！");
    }

    // 事务支持：删除班级，同时将该班学生设为“待分配班级”
    @Transactional
    @DeleteMapping("/classes/{id}")
    public Result<String> deleteClass(@PathVariable Long id) {
        manageMapper.clearStudentClassId(id);
        manageMapper.deleteClass(id);
        return Result.success("班级已删除，原班级学生已划转至【待分配班级】！");
    }

    // 课程列表
    @GetMapping("/courses")
    public Result<List<Course>> getCourses() {
        return Result.success(manageMapper.findAllCourses());
    }

    // 开设新课程
    @PostMapping("/courses")
    public Result<String> createCourse(@RequestBody Course course) {
        manageMapper.insertCourse(course);
        return Result.success("课程开设成功！");
    }

    // 事务支持：级联删除课程（自动清理下属所有实验任务及提交文件）
    @Transactional
    @DeleteMapping("/courses/{id}")
    public Result<String> deleteCourse(@PathVariable Long id) {
        manageMapper.deleteSubmissionsByCourseId(id);
        manageMapper.deleteTasksByCourseId(id);
        manageMapper.deleteCourse(id);
        return Result.success("课程及下属所有关联实验任务已彻底清理！");
    }

    // 学生名单管理 (支持班级联动与关键字搜索)
    @GetMapping("/students")
    public Result<List<User>> getStudents(@RequestParam(required = false) Long classId,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(manageMapper.findStudents(classId, keyword));
    }

    // 教师为学生调整/分配班级
    @PostMapping("/students/assign-class")
    public Result<String> assignClass(@RequestBody Map<String, Long> params) {
        Long studentId = params.get("studentId");
        Long classId = params.get("classId");
        manageMapper.updateStudentClass(studentId, classId);
        return Result.success("学生行政班级调整成功！");
    }
}