package com.yandex.app.model;

import java.util.Objects;

public class Task {
    private String nameOfTask;
    private String descriptionOfTask;
    private int id;
    private TaskStatus status;
    protected TypeOfTasks taskType;

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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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

    public Task(String nameOfTask, String descriptionOfTask, int id, TaskStatus status) {
        this.nameOfTask = nameOfTask;
        this.descriptionOfTask = descriptionOfTask;
        this.id = id;
        this.status = status;
    }

    public Task(int id, TypeOfTasks taskType, String nameOfTask, TaskStatus status, String descriptionOfTask) {
        this.id = id;
        this.taskType = taskType;
        this.nameOfTask = nameOfTask;
        this.status = status;
        this.descriptionOfTask = descriptionOfTask;
    }

    public Task copy() {
        return new Task(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus());
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", name=" + nameOfTask + ", description=" + descriptionOfTask + ", status=" +
                status + "]";
    }

    public TypeOfTasks getType() {
        return TypeOfTasks.TASK;
    }

    public void setId(int id) {
        this.id = id;
    }
}