package com.yandex.app.service;

import com.yandex.app.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;


    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try {
            StringBuilder content = new StringBuilder();
            String header = "id,type,name,status,description,epic";
            content.append(header).append("\n");

            for (Task task : tasks.values()) {
                content.append(toStringTask(task)).append("\n");
            }

            for (Epic epic : epics.values()) {
                content.append(toStringEpic(epic)).append("\n");

                for (Subtask subtask : subtasks.values()) {
                    if (subtask.getEpicId() == epic.getId()) {
                        content.append(toStringSubtask(subtask)).append("\n");
                    }
                }
            }

            Files.write(file.toPath(), content.toString().getBytes());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private String toStringTask(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getNameOfTask() + "," + task.getDescriptionOfTask() +
                "," + task.getStatus() + ",";
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
        int epicIdValue = 0;

        if (split.length > 5) {
            epicIdValue = Integer.parseInt(split[5]);
        }

        switch (type) {
            case TASK:
                return new Task(idValue, type, nameValue, statusValue, descriptionValue);
            case EPIC:
                return new Epic(idValue, type, nameValue, statusValue, descriptionValue);
            case SUBTASK:
                return new Subtask(idValue, type, nameValue, statusValue, descriptionValue, epicIdValue);
            default:
                throw new IllegalArgumentException("Неправильный тип: " + type);
        }
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
                    manager.subtasks.put(subtask.getId(), subtask);
                    break;
                default:
                    throw new IllegalStateException("Неправильный тип задачи: " + task.getType());
            }
        }
        return manager;
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