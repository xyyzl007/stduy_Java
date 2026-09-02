package com.example.study.mapper;

import com.example.study.entity.LabTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
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
}