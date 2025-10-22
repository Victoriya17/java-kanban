package com.yandex.app.model;

import com.yandex.app.service.InMemoryTaskManager;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String nameOfTask, String descriptionOfTask, int id, TaskStatus status, int epicId) {
        super(nameOfTask, descriptionOfTask, id, status);
        this.epicId = epicId;
    }

    public Subtask(int id, TypeOfTasks taskType, String nameOfTask, TaskStatus status, String descriptionOfTask,
                   int epicId) {
        super(id, taskType, nameOfTask, status, descriptionOfTask);
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
    public void setStatus(TaskStatus newStatus) {
        super.setStatus(newStatus);
        if (this.epicId > 0) {
            InMemoryTaskManager manager = new InMemoryTaskManager();
            manager.updateEpicStatus(this.epicId);
        }
    }

    @Override
    public Subtask copy() {
        return new Subtask(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus(), getEpicId());
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