package com.yandex.app.model;

import java.util.Objects;

public class Task {
    private String nameOfTask;
    private String descriptionOfTask;
    private int id;
    private TaskStatus status;

    public String getNameOfTask() {
        return nameOfTask;
    }

    public String setNameOfTask (String nameOfTask){
        this.nameOfTask = nameOfTask;
        return nameOfTask;
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

    public Task copy() {
        return new Task(nameOfTask, descriptionOfTask, id, status);
    }
}