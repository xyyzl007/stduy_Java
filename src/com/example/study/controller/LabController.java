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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    @Autowired
    private LabTaskMapper taskMapper;

    @Autowired
    private LabSubmissionMapper submissionMapper;

    // 1. 查询所有实验任务列表（启用缓存，避免高并发下频繁刷库）
    @Cacheable(value = "taskList")
    @GetMapping("/tasks")
    public Result<List<LabTask>> getTasks() {
        return Result.success(taskMapper.findAll());
    }

    // 2. 教师发布新实验任务（发布后自动清空缓存，保证数据一致性）
    @CacheEvict(value = "taskList", allEntries = true)
    @PostMapping("/tasks")
    public Result<String> createTask(@RequestBody LabTask task) {
        if (task.getCourseId() == null) task.setCourseId(1L);
        if (task.getMaxScore() == null) task.setMaxScore(100);
        taskMapper.insert(task);
        return Result.success("实验任务发布成功！");
    }

    // 3. 文件上传接口
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

    // 4. 上传后暂存为【草稿】
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

    // 5. 学生【正式确认提交】（扣减修改机会，限3次）
    @PostMapping("/confirm-submit")
    public Result<String> confirmSubmit(@RequestBody Map<String, Long> params) {
        Long taskId = params.get("taskId");
        Long studentId = params.get("studentId");

        LabTask task = taskMapper.findById(taskId);
        if (task != null && task.getDeadline().isBefore(LocalDateTime.now())) {
            return Result.error("该实验已截止，无法再提交！");
        }

        LabSubmission submission = submissionMapper.findByTaskAndStudent(taskId, studentId);
        if (submission == null) {
            return Result.error("未找到草稿文件，请先上传附件！");
        }
        if (submission.getSubmitCount() >= 3) {
            return Result.error("正式提交与修改机会已满 3 次，无法再次提交！");
        }

        submissionMapper.confirmSubmit(taskId, studentId);
        int remaining = 3 - (submission.getSubmitCount() + 1);
        return Result.success("正式提交成功！截止日期前您还剩 " + remaining + " 次修改机会。");
    }

    // 6. 学生查看自己的提交与草稿状态（顺便触发超期草稿自动提交）
    @GetMapping("/submissions/student/{studentId}")
    public Result<List<LabSubmission>> getStudentSubmissions(@PathVariable Long studentId) {
        submissionMapper.autoSubmitExpiredDrafts(); // 超期草稿自动提交
        return Result.success(submissionMapper.findByStudentId(studentId));
    }

    // 7. 教师查看指定实验任务的所有学生正式提交
    @GetMapping("/submissions/task/{taskId}")
    public Result<List<LabSubmission>> getTaskSubmissions(@PathVariable Long taskId) {
        submissionMapper.autoSubmitExpiredDrafts(); // 教师拉取时同步触发自动提交
        return Result.success(submissionMapper.findByTaskId(taskId));
    }

    // 8. 教师打分与评语
    @PostMapping("/grade")
    public Result<String> gradeReport(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Double score = Double.valueOf(body.get("score").toString());
        String feedback = (String) body.get("feedback");
        Long teacherId = Long.valueOf(body.get("teacherId").toString());

        submissionMapper.gradeReport(id, score, feedback, teacherId);
        return Result.success("批阅打分完成！");
    }

    // 9. 教师一键打包下载全部正式报告 (ZIP)
    @GetMapping("/submissions/task/{taskId}/export-zip")
    public void exportTaskSubmissionsZip(@PathVariable Long taskId, jakarta.servlet.http.HttpServletResponse response) {
        LabTask task = taskMapper.findById(taskId);
        String taskName = (task != null) ? task.getTitle() : "task_" + taskId;
        List<LabSubmission> list = submissionMapper.findByTaskId(taskId);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                java.net.URLEncoder.encode(taskName + "_学生报告汇总.zip", java.nio.charset.StandardCharsets.UTF_8));

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
            byte[] buffer = new byte[4096];

            for (LabSubmission sub : list) {
                if (sub.getFileUrl() == null) continue;
                String diskFileName = sub.getFileUrl().replace("/uploads/", "");
                File file = new File(uploadDir + diskFileName);
                if (!file.exists()) continue;

                String zipEntryName = (sub.getStudentName() != null ? sub.getStudentName() : "学生") + "_" + sub.getFileName();
                zos.putNextEntry(new java.util.zip.ZipEntry(zipEntryName));

                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
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