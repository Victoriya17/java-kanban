package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
        if (this.historyManager == null) {
            throw new IllegalStateException("HistoryManager не может быть null");
        }
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    private int getNextId() {
        return nextId++;
    }

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    protected Set<Task> prioritizedSet = new TreeSet<>(comparator);
    static Comparator<Task> comparator = Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId);

    @Override
    public Task addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task не может быть null");
        }

        int taskId = getNextId();
        task.setId(taskId);
        if (hasOverlappingTasks(task).isEmpty()) {
            tasks.put(taskId, task);
            if (task.getStartTime() != null) {
                prioritizedSet.add(task);
            }
            return task;
        } else {
            throw new IllegalArgumentException("Задача №" + taskId + " пересекается по времени с другой задачей");
        }
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        int subtaskId = getNextId();
        subtask.setId(subtaskId);
        if (hasOverlappingTasks(subtask).isEmpty()) {
            subtasks.put(subtaskId, subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtaskId(subtaskId);
                updateEpicStatus(epic.getId());
            } else {
                throw new IllegalArgumentException("Эпик с ID " + subtaskId + " не найден.");

            }
            if (subtask.getStartTime() != null) {
                prioritizedSet.add(subtask);
            }
            return subtask;
        } else {
            throw new IllegalArgumentException("Подзадача №" + subtaskId + " пересекается по времени с другой " +
                    "задачей");
        }
    }

    @Override
    public Epic addEpic(Epic epic) {
        int epicId = getNextId();
        epic.setId(epicId);
        if (hasOverlappingTasks(epic).isEmpty()) {
            epics.put(epicId, epic);
            if (epic.getStartTime() != null) {
                prioritizedSet.add(epic);
            }
            return epic;
        } else {
            throw new IllegalArgumentException("Эпик №" + epicId + " пересекается по времени с другой " +
                    "задачей");
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .toList();
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            prioritizedSet.remove(tasks.get(id));
        }
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            prioritizedSet.remove(subtasks.get(id));
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtask();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            prioritizedSet.remove(epics.get(id));
        }

        for (Epic epic : epics.values()) {
            deleteAllSubtasks();
        }
        epics.clear();
    }

    @Override
    public void deleteTaskById(Integer id) {
        prioritizedSet.remove(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        prioritizedSet.remove(subtasks.get(id));
        subtasks.remove(id);
    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epicToDelete = epics.get(epicId);
        if (epicToDelete != null) {
            List<Integer> subtasksIds = epicToDelete.getSubtasks();
            for (Integer subtaskId : subtasksIds) {
                Subtask subtask = subtasks.remove(subtaskId);
                prioritizedSet.remove(subtask);
                epicToDelete.removeSubtask(subtaskId);
            }
            prioritizedSet.remove(epics.get(epicId));
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
        if (hasOverlappingTasks(task).isEmpty()) {
            if (task.getStartTime() != null) {
                prioritizedSet.remove(tasks.get(task.getId()));
                prioritizedSet.add(task);
            }
            tasks.put(task.getId(), task);
        } else {
            throw new IllegalArgumentException("Обновленная задача №" + task.getId() + " пересекается по времени с " +
                    "другой задачей");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasOverlappingTasks(subtask).isEmpty()) {
            if (subtask.getStartTime() != null) {
                prioritizedSet.remove(subtasks.get(subtask.getId()));
                prioritizedSet.add(subtask);
            }
            subtasks.put(subtask.getId(), subtask);
            updateSubtaskStatus(subtask.getId(), subtask.getStatus());
        } else {
            throw new IllegalArgumentException("Обновленная подзадача №" + subtask.getId() + " пересекается по " +
                    "времени с другой задачей");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (hasOverlappingTasks(epic).isEmpty()) {
            if (epic.getStartTime() != null) {
                prioritizedSet.remove(epics.get(epic.getId()));
                prioritizedSet.add(epic);
            }
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic.getId());
        } else {
            throw new IllegalArgumentException("Обновленный эпик №" + epic.getId() + " пересекается по " +
                    "времени с другой задачей");
        }
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

        List<Subtask> epicSubtasks = getAllSubtasks(epicId);
        if (epicSubtasks.isEmpty() || allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        for (Subtask subtask : getAllSubtasks(epicId)) {
            prioritizedSet.remove(subtask);
            if (subtask.getStartTime() != null) {
                prioritizedSet.add(subtask);
            }
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
        prioritizedSet.remove(subtask);
        prioritizedSet.add(subtask);
    }

    public List<Task> getPrioritizedTasks() {
        return prioritizedSet.stream().filter(Objects::nonNull).filter(Task ->
                Task.getStartTime() != null).toList();
    }

    private boolean isOverlapping(Task task, Task prioritizedTask) {
        if (prioritizedTask.getStartTime() == null || prioritizedTask.getEndTime() == null ||
                task.getEndTime() == null) {
            return false;
        }

        return !(task.getEndTime().isBefore(prioritizedTask.getStartTime()) ||
                prioritizedTask.getEndTime().isBefore(task.getStartTime()));
    }

    public Optional<Task> hasOverlappingTasks(Task task) {
        return getPrioritizedTasks().stream()
                .filter(prioritizedTask -> isOverlapping(task, prioritizedTask))
                .findFirst();
    }
}