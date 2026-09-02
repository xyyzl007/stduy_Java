package com.example.study.entity;

import java.time.LocalDateTime;

public class LabTask {
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private String templateUrl;
    private LocalDateTime deadline;
    private Integer maxScore;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTemplateUrl() { return templateUrl; }
    public void setTemplateUrl(String templateUrl) { this.templateUrl = templateUrl; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
}