package com.example.study.controller;

import com.example.study.common.Result;
import com.example.study.entity.LabSubmission;
import com.example.study.entity.LabTask;
import com.example.study.mapper.LabSubmissionMapper;
import com.example.study.mapper.LabTaskMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    @Autowired
    private LabTaskMapper taskMapper;

    @Autowired
    private LabSubmissionMapper submissionMapper;

    // 1. 查询所有实验任务（缓存）
    @Cacheable(value = "taskList")
    @GetMapping("/tasks")
    public Result<List<LabTask>> getTasks() {
        return Result.success(taskMapper.findAll());
    }

    // 2. 教师发布新实验（清空缓存）
    @CacheEvict(value = "taskList", allEntries = true)
    @PostMapping("/tasks")
    public Result<String> createTask(@RequestBody LabTask task) {
        if (task.getCourseId() == null) task.setCourseId(1L);
        if (task.getMaxScore() == null) task.setMaxScore(100);
        taskMapper.insert(task);
        return Result.success("实验任务发布成功！");
    }

    // 【解决问题一】：教师修改实验任务（清空缓存）
    @CacheEvict(value = "taskList", allEntries = true)
    @PutMapping("/tasks")
    public Result<String> updateTask(@RequestBody LabTask task) {
        taskMapper.update(task);
        return Result.success("实验任务修改成功！");
    }

    @CacheEvict(value = "taskList", allEntries = true)
    @DeleteMapping("/tasks/{id}")
    public Result<String> deleteTask(@PathVariable Long id) {
        taskMapper.deleteSubmissionsByTaskId(id); // 先清理提交附件
        taskMapper.deleteById(id);                // 再删除实验任务
        return Result.success("实验任务及关联学生提交已彻底删除！");
    }

    // 5. 上传附件
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
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

    // 6. 暂存草稿
    @PostMapping("/draft")
    public Result<String> saveDraft(@RequestBody LabSubmission submission) {
        LabTask task = taskMapper.findById(submission.getTaskId());
        if (task != null && task.getDeadline().isBefore(LocalDateTime.now())) {
            return Result.error("该实验任务已过截止时间，无法上传！");
        }
        LabSubmission exist = submissionMapper.findByTaskAndStudent(submission.getTaskId(), submission.getStudentId());
        if (exist != null && exist.getSubmitCount() >= 3) {
            return Result.error("您已用完 3 次正式提交/修改机会，无法再更新草稿！");
        }
        submissionMapper.saveDraft(submission);
        return Result.success("草稿保存成功！请在截止时间前确认正式提交。");
    }

    // 7. 学生正式提交
    @PostMapping("/confirm-submit")
    public Result<String> confirmSubmit(@RequestBody Map<String, Long> params) {
        Long taskId = params.get("taskId");
        Long studentId = params.get("studentId");

        LabTask task = taskMapper.findById(taskId);
        if (task != null && task.getDeadline().isBefore(LocalDateTime.now())) {
            return Result.error("该实验已截止，无法再提交！");
        }

        LabSubmission submission = submissionMapper.findByTaskAndStudent(taskId, studentId);
        if (submission == null) return Result.error("未找到草稿文件，请先上传附件！");
        if (submission.getSubmitCount() >= 3) return Result.error("正式提交与修改机会已满 3 次，无法再次提交！");

        submissionMapper.confirmSubmit(taskId, studentId);
        int remaining = 3 - (submission.getSubmitCount() + 1);
        return Result.success("正式提交成功！截止日期前您还剩 " + remaining + " 次修改机会。");
    }

    // 8. 学生查询自己记录
    @GetMapping("/submissions/student/{studentId}")
    public Result<List<LabSubmission>> getStudentSubmissions(@PathVariable Long studentId) {
        submissionMapper.autoSubmitExpiredDrafts();
        return Result.success(submissionMapper.findByStudentId(studentId));
    }

    // 9. 教师查询任务提交列表
    @GetMapping("/submissions/task/{taskId}")
    public Result<List<LabSubmission>> getTaskSubmissions(@PathVariable Long taskId) {
        submissionMapper.autoSubmitExpiredDrafts();
        return Result.success(submissionMapper.findByTaskId(taskId));
    }

    // 10. 教师打分
    @PostMapping("/grade")
    public Result<String> gradeReport(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Double score = Double.valueOf(body.get("score").toString());
        String feedback = (String) body.get("feedback");
        Long teacherId = Long.valueOf(body.get("teacherId").toString());

        submissionMapper.gradeReport(id, score, feedback, teacherId);
        return Result.success("批阅打分完成！");
    }

    // 11. 一键打包 ZIP
    @GetMapping("/submissions/task/{taskId}/export-zip")
    public void exportTaskSubmissionsZip(@PathVariable Long taskId, HttpServletResponse response) {
        LabTask task = taskMapper.findById(taskId);
        String taskName = (task != null) ? task.getTitle() : "task_" + taskId;
        List<LabSubmission> list = submissionMapper.findByTaskId(taskId);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                URLEncoder.encode(taskName + "_学生报告汇总.zip", StandardCharsets.UTF_8));

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
            byte[] buffer = new byte[4096];

            for (LabSubmission sub : list) {
                if (sub.getFileUrl() == null) continue;
                String diskFileName = sub.getFileUrl().replace("/uploads/", "");
                File file = new File(uploadDir + diskFileName);
                if (!file.exists()) continue;

                String zipEntryName = (sub.getStudentName() != null ? sub.getStudentName() : "学生") + "_" + sub.getFileName();
                zos.putNextEntry(new ZipEntry(zipEntryName));

                try (FileInputStream fis = new FileInputStream(file)) {
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
            zos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}