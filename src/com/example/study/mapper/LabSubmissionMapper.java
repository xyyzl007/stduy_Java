package com.example.study.mapper;

import com.example.study.entity.LabSubmission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface LabSubmissionMapper {

    @Select("SELECT s.*, u.real_name as studentName, t.title as taskTitle " +
            "FROM lab_submission s " +
            "LEFT JOIN sys_user u ON s.student_id = u.id " +
            "LEFT JOIN lab_task t ON s.task_id = t.id " +
            "WHERE s.task_id = #{taskId} ORDER BY s.submit_time DESC")
    List<LabSubmission> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT s.*, u.real_name as studentName, t.title as taskTitle " +
            "FROM lab_submission s " +
            "LEFT JOIN sys_user u ON s.student_id = u.id " +
            "LEFT JOIN lab_task t ON s.task_id = t.id " +
            "WHERE s.student_id = #{studentId}")
    List<LabSubmission> findByStudentId(@Param("studentId") Long studentId);

    @Insert("INSERT INTO lab_submission(task_id, student_id, file_name, file_url, file_size, status) " +
            "VALUES(#{taskId}, #{studentId}, #{fileName}, #{fileUrl}, #{fileSize}, 'SUBMITTED') " +
            "ON DUPLICATE KEY UPDATE file_name=VALUES(file_name), file_url=VALUES(file_url), " +
            "file_size=VALUES(file_size), status='SUBMITTED', submit_time=NOW(), score=NULL, feedback=NULL")
    int submitReport(LabSubmission submission);

    @Update("UPDATE lab_submission SET score = #{score}, feedback = #{feedback}, " +
            "graded_by = #{teacherId}, status = 'GRADED', graded_at = NOW() WHERE id = #{id}")
    int gradeReport(@Param("id") Long id,
                    @Param("score") Double score,
                    @Param("feedback") String feedback,
                    @Param("teacherId") Long teacherId);
}