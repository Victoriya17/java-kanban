package com.yandex.app.service;

import com.yandex.app.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;


    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try {
            StringBuilder content = new StringBuilder();
            String header = "id,type,name,status,description,epic,duration,start_time,end_time";
            content.append(header).append("\n");

            for (Task task : tasks.values()) {
                content.append(toStringTask(task)).append("\n");
            }

            for (Epic epic : epics.values()) {
                content.append(toStringEpic(epic)).append("\n");
            }

            for (Subtask subtask : subtasks.values()) {
                Epic epic = epics.get(subtask.getEpicId());
                if (epic != null) {
                    content.append(toStringSubtask(subtask)).append("\n");
                } else {
                    throw new IllegalStateException("Эпик для подзадачи не найден: " + subtask.getId());
                }
            }

            Files.write(file.toPath(), content.toString().getBytes());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private String toStringTask(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getNameOfTask() + "," + task.getDescriptionOfTask() +
                "," + task.getStatus() + "," + task.getDurationToMinutes() + "," + task.getStartTime() + "," +
                task.getEndTime() + ",";
    }

    private String toStringEpic(Epic epic) {
        return toStringTask(epic);
    }

    private String toStringSubtask(Subtask subtask) {
        return toStringTask(subtask) + subtask.getEpicId();
    }

    private static Task fromString(String value) throws IllegalArgumentException {
        String[] split = value.split(",");
        int idValue = Integer.parseInt(split[0]);
        TypeOfTasks type = TypeOfTasks.valueOf(split[1]);
        String nameValue = split[2];
        String descriptionValue = split[3];
        TaskStatus statusValue = TaskStatus.valueOf(split[4]);
        long durationValue = Long.parseLong(split[5]);
        LocalDateTime startTimeValue = LocalDateTime.parse(split[6]);

        int epicIdValue = 0;

        if (split.length > 8) {
            epicIdValue = Integer.parseInt(split[8]);
        }

        Task task;
        switch (type) {
            case TASK:
                task = new Task(nameValue, statusValue, descriptionValue, durationValue, startTimeValue);
                break;
            case EPIC:
                task = new Epic(nameValue, statusValue, descriptionValue, durationValue, startTimeValue);
                break;
            case SUBTASK:
                task = new Subtask(nameValue, statusValue, descriptionValue, durationValue,
                        startTimeValue, epicIdValue);
                break;
            default:
                throw new IllegalArgumentException("Неправильный тип: " + type);
        }

        task.setId(idValue);
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("Файл не найден: " + file.getPath());
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        StringBuilder content = new StringBuilder();
        try {
            content.append(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new IOException("Ошибка при чтении файла: " + e.getMessage());
        }

        int maxId = 0;
        String[] lines = content.toString().split("\n");
        for (int i = 1; i < lines.length; i++) {
            Task task = fromString(lines[i]);

            if (task.getId() > maxId) {
                maxId = task.getId();
            }

            manager.prioritizedSet.add(task);
            processTask(manager, task);
        }
        return manager;
    }

    private static void processTask(FileBackedTaskManager manager, Task task) {
        switch (task.getType()) {
            case TASK:
                manager.tasks.put(task.getId(), task);
                break;
            case EPIC:
                Epic epic = (Epic) task;
                manager.epics.put(epic.getId(), epic);
                break;
            case SUBTASK:
                Subtask subtask = (Subtask) task;
                Epic epicForSubtask = manager.epics.get(subtask.getEpicId());
                if (epicForSubtask != null) {
                    subtask.setEpicId(epicForSubtask.getId());
                    manager.subtasks.put(subtask.getId(), subtask);
                    epicForSubtask.addSubtaskId(subtask.getId());
                } else {
                    throw new IllegalStateException("Эпик для подзадачи не найден: " + subtask.getId());
                }
                break;
            default:
                throw new IllegalStateException("Неправильный тип задачи: " + task.getType());
        }
    }

    @Override
    public Task addTask(Task task) {
        Task newTask = super.addTask(task);
        save();
        return newTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic newEpic = super.addEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask newSubtask = super.addSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }
}