package com.example.study.mapper;

import com.example.study.entity.LabTask;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LabTaskMapper {

    @Select("SELECT * FROM lab_task ORDER BY id DESC")
    List<LabTask> findAll();

    @Select("SELECT * FROM lab_task WHERE id = #{id}")
    LabTask findById(Long id);

    @Insert("INSERT INTO lab_task(course_id, title, content, deadline, max_score) " +
            "VALUES(#{courseId}, #{title}, #{content}, #{deadline}, #{maxScore})")
    int insert(LabTask task);

    @Update("UPDATE lab_task SET course_id = #{courseId}, title = #{title}, content = #{content}, " +
            "deadline = #{deadline}, max_score = #{maxScore} WHERE id = #{id}")
    int update(LabTask task);

    // 级联删除：先清理该任务下所有学生的提交记录，杜绝外键约束报错
    @Delete("DELETE FROM lab_submission WHERE task_id = #{id}")
    int deleteSubmissionsByTaskId(Long id);

    // 再删除实验任务本身
    @Delete("DELETE FROM lab_task WHERE id = #{id}")
    int deleteById(Long id);
}