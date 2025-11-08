package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected String nameOfTask;
    protected String descriptionOfTask;
    protected int id;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String nameOfTask, String descriptionOfTask, TaskStatus status, long duration,
                LocalDateTime startTime) {
        this.nameOfTask = nameOfTask;
        this.descriptionOfTask = descriptionOfTask;
        this.status = status;
        this.duration = Duration.ofMinutes(duration);
        this.startTime = startTime;
    }

    public Task(String nameOfTask, String descriptionOfTask, TaskStatus status) {
        this(nameOfTask, descriptionOfTask, status, 0, null);
    }

    public String getNameOfTask() {
        return nameOfTask;
    }

    public void setNameOfTask(String nameOfTask) {
        this.nameOfTask = nameOfTask;
    }

    public String getDescriptionOfTask() {
        return descriptionOfTask;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task otherTask = (Task) obj;
        return Objects.equals(id, otherTask.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Task copy() {
        Task copy = new Task(nameOfTask, descriptionOfTask, status);
        copy.id = this.id;
        copy.duration = this.duration;
        copy.startTime = this.startTime;
        return copy;
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", name=" + nameOfTask + ", description=" + descriptionOfTask + ", status=" +
                status + "]";
    }

    public TypeOfTasks getType() {
        return TypeOfTasks.TASK;
    }

    public long getDurationToMinutes() {
        if (duration == null) {
            return 0L;
        }
        return duration.toMinutes();
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }

        return startTime.plusMinutes(getDurationToMinutes());
    }
}