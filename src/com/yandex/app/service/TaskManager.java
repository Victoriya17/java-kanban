package com.yandex.app.service;

import com.yandex.app.exceptions.TimeOverlapException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.List;

public interface TaskManager {
    Task addTask(Task task) throws TimeOverlapException;

    Subtask addSubtask(Subtask subtask) throws TimeOverlapException;

    Epic addEpic(Epic epic) throws TimeOverlapException;

    List<Task> getAllTasks();

    List<Subtask> getEpicSubtasks(int epicId);

    List<Subtask> getEpicSubtasks();

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

    void updateTask(Task task) throws TimeOverlapException;

    void updateSubtask(Subtask subtask) throws TimeOverlapException;

    void updateEpic(Epic epic) throws TimeOverlapException;

    List<Task> getPrioritizedTasks();

    List<Task> getHistory();
}