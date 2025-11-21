package com.yandex.app.service;

import com.yandex.app.exceptions.TimeOverlapException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    private final HistoryManager historyManager;
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected Set<Task> prioritizedSet = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));

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

    @Override
    public Task addTask(Task task) throws TimeOverlapException {
        if (task == null) {
            throw new IllegalArgumentException("Task не может быть null");
        }

        int taskId = getNextId();
        task.setId(taskId);

        if (!hasOverlappingTasks(task).isEmpty()) {
            throw new TimeOverlapException("Задача №" + taskId + " пересекается по времени с другой задачей");
        }

        tasks.put(taskId, task);
        if (task.getStartTime() != null) {
            prioritizedSet.add(task);
        }
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) throws TimeOverlapException {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask не может быть null");
        }

        int subtaskId = getNextId();
        subtask.setId(subtaskId);

        if (!hasOverlappingTasks(subtask).isEmpty()) {
            throw new TimeOverlapException("Подзадача №" + subtaskId + " пересекается по времени с другой " +
                    "задачей/подзадачей");
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден");
        }

        subtasks.put(subtaskId, subtask);
        epic.addSubtaskId(subtaskId);

        updateDurationAndTimes(epic);
        updateEpicStatus(epic.getId());

        if (subtask.getStartTime() != null) {
            prioritizedSet.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) throws TimeOverlapException {
        if (epic == null) {
            throw new IllegalArgumentException("Epic не может быть null");
        }

        int epicId = getNextId();
        epic.setId(epicId);

        if (!hasOverlappingTasks(epic).isEmpty()) {
            throw new TimeOverlapException("Эпик №" + epicId + " пересекается по времени с другой задачей");
        }

        epics.put(epicId, epic);
        System.out.println("Эпик ID " + epicId + " успешно добавлен");
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .toList();
    }

    @Override
    public List<Subtask> getEpicSubtasks() {
        return new ArrayList<>(subtasks.values());
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
            updateDurationAndTimes(epic);
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
        prioritizedSet.remove(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            System.out.println("Подзадача с ID " + id + " не найдена");
            return;
        }

        prioritizedSet.remove(subtasks.get(id));
        subtasks.remove(id);

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic != null) {
            epic.removeSubtask(id);
            updateDurationAndTimes(epic);
            updateEpicStatus(epic.getId());
        } else {
            System.out.println("Эпик с ID " + epicId + " не найден");
        }

        System.out.println("Подзадача с ID " + id + " успешно удалена");
    }

    @Override
    public void deleteEpicById(int epicId) {
        try {
            Epic epicToDelete = epics.get(epicId);
            if (epicToDelete == null) {
                System.out.println("Эпик с ID " + epicId + " не найдена");
                return;
            }

            List<Integer> subtasksIds = epicToDelete.getSubtasks();
            if (subtasksIds != null) {
                for (Integer subtaskId : subtasksIds) {
                    subtasks.remove(subtaskId);
                    epicToDelete.removeSubtask(subtaskId);
                }
            }
            epics.remove(epicId);
        } catch (Exception e) {
            System.out.println("Ошибка при удалении эпика " + epicId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
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
    public void updateTask(Task task) throws TimeOverlapException {
        if (hasOverlappingTasks(task).isEmpty()) {
            if (task.getStartTime() != null) {
                prioritizedSet.remove(tasks.get(task.getId()));
                prioritizedSet.add(task);
            }
            tasks.put(task.getId(), task);
        } else {
            throw new TimeOverlapException("Обновленная задача №" + task.getId() + " пересекается по времени с " +
                    "другой задачей");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TimeOverlapException {
        if (hasOverlappingTasks(subtask).isEmpty()) {
            if (subtask.getStartTime() != null) {
                prioritizedSet.remove(subtasks.get(subtask.getId()));
                prioritizedSet.add(subtask);
            }
            subtasks.put(subtask.getId(), subtask);

            Epic epic = epics.get(subtask.getEpicId());
            updateEpicStatus(epic.getId());
        } else {
            throw new TimeOverlapException("Обновленная подзадача №" + subtask.getId() + " пересекается по " +
                    "времени с другой задачей");
        }
    }

    @Override
    public void updateEpic(Epic epic) throws TimeOverlapException {
        if (hasOverlappingTasks(epic).isEmpty()) {
            if (epic.getStartTime() != null) {
                prioritizedSet.remove(epics.get(epic.getId()));
                prioritizedSet.add(epic);
            }
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic.getId());
        } else {
            throw new TimeOverlapException("Обновленный эпик №" + epic.getId() + " пересекается по " +
                    "времени с другой задачей");
        }
    }

    private void updateEpicStatus(int epicId) {
        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : getEpicSubtasks(epicId)) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        Epic epic = epics.get(epicId);

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty() || allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedSet);
    }

    private boolean isOverlapping(Task task, Task prioritizedTask) {
        if (prioritizedTask.getStartTime() == null || prioritizedTask.getEndTime() == null ||
                task.getEndTime() == null) {
            return false;
        }

        return task.getEndTime().isAfter(prioritizedTask.getStartTime()) &&
                prioritizedTask.getEndTime().isAfter(task.getStartTime());
    }

    private Optional<Task> hasOverlappingTasks(Task task) {
        return getPrioritizedTasks().stream()
                .filter(prioritizedTask -> isOverlapping(task, prioritizedTask))
                .findFirst();
    }

    protected void updateDurationAndTimes(Epic epic) {
        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStartTime = LocalDateTime.MAX;
        LocalDateTime latestEndTime = LocalDateTime.MIN;

        List<Integer> subtaskIds = epic.getSubtasks();

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            totalDuration = totalDuration.plus(subtask.getDuration());

            if (subtask.getStartTime() != null && subtask.getStartTime().isBefore(earliestStartTime)) {
                earliestStartTime = subtask.getStartTime();
            }

            LocalDateTime subtaskEndTime = subtask.getEndTime();
            if (subtaskEndTime != null && subtaskEndTime.isAfter(latestEndTime)) {
                latestEndTime = subtaskEndTime;
            }
        }

        if (earliestStartTime == LocalDateTime.MAX) {
            epic.setStartTime(null);
        } else {
            epic.setStartTime(earliestStartTime);
        }
        if (latestEndTime == LocalDateTime.MIN) {
            epic.setEndTime(null);
        } else {
            epic.setEndTime(latestEndTime);
        }
        epic.setDuration(totalDuration);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}