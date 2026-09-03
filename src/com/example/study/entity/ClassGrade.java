package com.example.study.entity;

import java.time.LocalDateTime;

public class ClassGrade {
    private Long id;
    private String className;
    private String gradeYear;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGradeYear() { return gradeYear; }
    public void setGradeYear(String gradeYear) { this.gradeYear = gradeYear; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}