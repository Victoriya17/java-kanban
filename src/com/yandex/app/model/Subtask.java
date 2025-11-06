package com.yandex.app.model;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String nameOfTask, String descriptionOfTask, TaskStatus status, int epicId) {
        super(nameOfTask, descriptionOfTask, status);
        this.epicId = epicId;
    }

    public Subtask(String nameOfTask, TaskStatus status, String descriptionOfTask, long duration,
                   LocalDateTime startTime, int epicId) {
        super(nameOfTask, status, descriptionOfTask, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId != getId()) {
            this.epicId = epicId;
        }
    }

    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(getNameOfTask(), getDescriptionOfTask(), getStatus(), getEpicId());
        copy.setId(this.getId());
        copy.duration = this.duration;
        copy.startTime = this.startTime;
        return copy;
    }

    @Override
    public String toString() {
        return "Subtask [id=" + getId() + ", name=" + getNameOfTask() + ", description=" + getDescriptionOfTask() +
                ", status=" + getStatus() + ", epicId=" + epicId + "]";
    }

    @Override
    public TypeOfTasks getType() {
        return TypeOfTasks.SUBTASK;
    }
}