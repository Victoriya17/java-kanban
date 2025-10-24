package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    public HistoryManager historyManager = Managers.getDefaultHistory();

    public static int getNextId() {
        return nextId++;
    }

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    @Override
    public Task addTask(Task task) {
        int taskId = getNextId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        int subtaskId = getNextId();
        subtask.setId(subtaskId);
        subtasks.put(subtaskId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtaskId);
            updateEpicStatus(epic.getId());
        } else {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден.");

        }
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        int epicId = getNextId();
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks(int epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtask();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            deleteAllSubtasks();
        }
        epics.clear();
    }

    @Override
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        subtasks.remove(id);
    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epicToDelete = epics.get(epicId);
        if (epicToDelete != null) {
            List<Integer> subtasksIds = epicToDelete.getSubtasks();
            for (Integer subtaskId : subtasksIds) {
                subtasks.remove(subtaskId);
                epicToDelete.removeSubtask(subtaskId);
            }
            epics.remove(epicId);
            updateEpicStatus(epicToDelete.getId());
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            Task copiedTask = task.copy();
            historyManager.add(copiedTask);
            return copiedTask;
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Subtask copiedSubtask = subtask.copy();
            historyManager.add(copiedSubtask);
            return copiedSubtask;
        }
        return null;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            Epic copiedEpic = epic.copy();
            historyManager.add(copiedEpic);
            return copiedEpic;
        }
        return null;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateSubtaskStatus(subtask.getId(), subtask.getStatus());
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    public void updateEpicStatus(int epicId) {
        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : getAllSubtasks(epicId)) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        Epic epic = epics.get(epicId);

        if (subtasks.isEmpty() || allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public void updateSubtaskStatus(int subtaskId, TaskStatus newStatus) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            subtask.setStatus(newStatus);
            int epicId = subtask.getEpicId();
            updateEpicStatus(epicId);
        }
    }
}