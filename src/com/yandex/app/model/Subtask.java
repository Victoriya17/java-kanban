package com.yandex.app.model;

import com.yandex.app.service.InMemoryTaskManager;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String nameOfTask, String descriptionOfTask, int id, TaskStatus status, Epic epic) {
        super(nameOfTask, descriptionOfTask, id, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        if (epic != this.epic) {
            this.epic = epic;
        }
    }

    @Override
    public void setStatus(TaskStatus newStatus) {
        super.setStatus(newStatus);
        if (this.epic != null) {
            InMemoryTaskManager manager = new InMemoryTaskManager();
            manager.updateEpicStatus(this.epic);
        }
    }

    @Override
    public Subtask copy() {
        return new Subtask(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus(), epic);
    }

    @Override
    public String toString() {
        return "Subtask [id=" + getId() + ", name=" + getNameOfTask() + ", description=" + getDescriptionOfTask() +
                ", status=" + getStatus() + ", epic=" + epic + "]";
    }
}
