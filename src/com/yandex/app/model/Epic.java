package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String nameOfTask, String descriptionOfTask, int id, TaskStatus status) {
        super(nameOfTask, descriptionOfTask, id, status);
    }

    public Epic(int id, TypeOfTasks taskType, String nameOfTask, TaskStatus status, String descriptionOfTask) {
        super(id, taskType, nameOfTask, status, descriptionOfTask);
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
        return new Epic(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus());
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
}