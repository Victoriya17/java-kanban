package com.yandex.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();
    protected LocalDateTime endTime;

    public Epic(String nameOfTask, String descriptionOfTask, TaskStatus status) {
        super(nameOfTask, descriptionOfTask, status);
    }

    public Epic(String nameOfTask, TaskStatus status, String descriptionOfTask, long duration, LocalDateTime startTime) {
        super(nameOfTask, status, descriptionOfTask, duration, startTime);
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId != this.getId()) {
            subtasksIds.add(subtaskId);
        }
    }

    public List<Integer> getSubtasks() {
        return List.copyOf(subtasksIds);
    }

    public void removeSubtask(int subtaskId) {
        subtasksIds.remove(subtaskId);
    }

    public void deleteAllSubtask() {
        subtasksIds.clear();
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(getNameOfTask(), getDescriptionOfTask(), getStatus());
        copy.setId(this.getId());
        copy.duration = this.duration;
        copy.startTime = this.startTime;
        return copy;
    }

    @Override
    public String toString() {
        return "Epic [id=" + getId() + ", name=" + getNameOfTask() + ", description=" + getDescriptionOfTask() + ", " +
                "status=" + getStatus() + "]";
    }

    @Override
    public TypeOfTasks getType() {
        return TypeOfTasks.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}