package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

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
        updateDurationAndTimes();
    }

    public void deleteAllSubtask() {
        subtasksIds.clear();
        updateDurationAndTimes();
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

    protected void updateDurationAndTimes() {
        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;

        for (Task subtask : subtasks) {
            totalDuration = totalDuration.plus(subtask.getDuration());

            if (subtask.getStartTime() != null
                    && (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime))) {
                earliestStartTime = subtask.getStartTime();
            }

            LocalDateTime subtaskEndTime = subtask.getEndTime();
            if (subtaskEndTime != null
                    && (latestEndTime == null || subtaskEndTime.isAfter(latestEndTime))) {
                latestEndTime = subtaskEndTime;
            }
        }

        this.duration = totalDuration;
        this.startTime = earliestStartTime;
        this.endTime = latestEndTime;
    }
}