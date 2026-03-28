package ru.naujava.taskmanager.model.ydb;

import java.time.Instant;

/**
 * Модель задачи для YDB.
 */
public class YdbTask {
    private Long id;
    private String description;
    private Boolean isDone;
    private Instant createdAt;
    private Instant updatedAt;
    private Long userId;

    public YdbTask() {
    }

    public YdbTask(Long id, String description, Boolean isDone,
                   Instant createdAt, Instant updatedAt, Long userId) {
        this.id = id;
        this.description = description;
        this.isDone = isDone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(Boolean isDone) {
        this.isDone = isDone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}