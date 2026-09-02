package com.example.study.controller;

import com.example.study.common.Result;
import com.example.study.entity.LabSubmission;
import com.example.study.entity.LabTask;
import com.example.study.mapper.LabSubmissionMapper;
import com.example.study.mapper.LabTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    @Autowired
    private LabTaskMapper taskMapper;

    @Autowired
    private LabSubmissionMapper submissionMapper;

    // 1. 查询所有实验任务列表
    @GetMapping("/tasks")
    public Result<List<LabTask>> getTasks() {
        return Result.success(taskMapper.findAll());
    }

    // 2. 文件上传接口 (保存到本地 uploads 目录)
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String uniqueName = UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + uniqueName));
            return Result.success("/uploads/" + uniqueName);
        } catch (IOException e) {
            return Result.error("文件保存失败: " + e.getMessage());
        }
    }

    // 3. 学生提交实验报告
    @PostMapping("/submit")
    public Result<String> submitReport(@RequestBody LabSubmission submission) {
        submissionMapper.submitReport(submission);
        return Result.success("报告提交成功！");
    }

    // 4. 学生查看自己的提交历史
    @GetMapping("/submissions/student/{studentId}")
    public Result<List<LabSubmission>> getStudentSubmissions(@PathVariable Long studentId) {
        return Result.success(submissionMapper.findByStudentId(studentId));
    }

    // 5. 教师查看某个实验任务的所有学生提交
    @GetMapping("/submissions/task/{taskId}")
    public Result<List<LabSubmission>> getTaskSubmissions(@PathVariable Long taskId) {
        return Result.success(submissionMapper.findByTaskId(taskId));
    }

    // 6. 教师打分与写评语
    @PostMapping("/grade")
    public Result<String> gradeReport(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Double score = Double.valueOf(body.get("score").toString());
        String feedback = (String) body.get("feedback");
        Long teacherId = Long.valueOf(body.get("teacherId").toString());

        submissionMapper.gradeReport(id, score, feedback, teacherId);
        return Result.success("批阅打分完成！");
    }
}