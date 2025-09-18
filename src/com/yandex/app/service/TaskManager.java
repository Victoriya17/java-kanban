package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.util.List;

public interface TaskManager {
    Task addTask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus);

    Subtask addSubtask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus, Epic epic);

    Epic addEpic(String nameOfTask, String descriptionOfTask, TaskStatus status);

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks(int epicId);

    List<Epic> getAllEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteTaskById(Integer id);

    void deleteSubtaskById(Integer id);

    void deleteEpicById(int epicId);

    Task getTaskById(Integer id);

    Subtask getSubtaskById(Integer id);

    Epic getEpicById(Integer id);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void updateEpicStatus(Epic epic);

    void updateSubtaskStatus(int subtaskId, TaskStatus newStatus);
}
