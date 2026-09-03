package com.example.study.mapper;

import com.example.study.entity.LabSubmission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LabSubmissionMapper {

    // 教师查看列表：只展示正式提交(SUBMITTED)和已批阅(GRADED)的报告，不看学生的未确认草稿
    @Select("SELECT s.*, u.real_name as studentName, t.title as taskTitle " +
            "FROM lab_submission s " +
            "LEFT JOIN sys_user u ON s.student_id = u.id " +
            "LEFT JOIN lab_task t ON s.task_id = t.id " +
            "WHERE s.task_id = #{taskId} AND s.status != 'DRAFT' ORDER BY s.submit_time DESC")
    List<LabSubmission> findByTaskId(@Param("taskId") Long taskId);

    // 查询学生个人的所有记录（包含草稿与正式提交）
    @Select("SELECT s.*, u.real_name as studentName, t.title as taskTitle " +
            "FROM lab_submission s " +
            "LEFT JOIN sys_user u ON s.student_id = u.id " +
            "LEFT JOIN lab_task t ON s.task_id = t.id " +
            "WHERE s.student_id = #{studentId}")
    List<LabSubmission> findByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM lab_submission WHERE task_id = #{taskId} AND student_id = #{studentId}")
    LabSubmission findByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    // 保存为草稿（不消耗正式提交次数）
    @Insert("INSERT INTO lab_submission(task_id, student_id, file_name, file_url, file_size, status, submit_count) " +
            "VALUES(#{taskId}, #{studentId}, #{fileName}, #{fileUrl}, #{fileSize}, 'DRAFT', 0) " +
            "ON DUPLICATE KEY UPDATE file_name=VALUES(file_name), file_url=VALUES(file_url), " +
            "file_size=VALUES(file_size), status='DRAFT'")
    int saveDraft(LabSubmission submission);

    // 正式确认提交（提交次数+1）
    @Update("UPDATE lab_submission SET status = 'SUBMITTED', submit_time = NOW(), " +
            "submit_count = submit_count + 1, score = NULL, feedback = NULL " +
            "WHERE task_id = #{taskId} AND student_id = #{studentId}")
    int confirmSubmit(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    // 截止日期到达时，自动将超时的草稿转为正式提交
    @Update("UPDATE lab_submission s " +
            "INNER JOIN lab_task t ON s.task_id = t.id " +
            "SET s.status = 'SUBMITTED', s.submit_count = s.submit_count + 1, s.submit_time = NOW() " +
            "WHERE s.status = 'DRAFT' AND t.deadline <= NOW()")
    int autoSubmitExpiredDrafts();

    // 教师打分
    @Update("UPDATE lab_submission SET score = #{score}, feedback = #{feedback}, " +
            "graded_by = #{teacherId}, status = 'GRADED', graded_at = NOW() WHERE id = #{id}")
    int gradeReport(@Param("id") Long id, @Param("score") Double score,
                    @Param("feedback") String feedback, @Param("teacherId") Long teacherId);
}