package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task newTask = inMemoryTaskManager.addTask(new Task("Почистить фильтр", "Фильтр " +
                "бризера надо чистить 2 раза в месяц", TaskStatus.NEW));
        inMemoryTaskManager.getTaskById(newTask.getId());
        inMemoryTaskManager.addTask(new Task("Помыть полы", "Мыть полы надо 4 раза в месяц",
                TaskStatus.NEW));
        Epic cleaning = inMemoryTaskManager.addEpic(new Epic("Уборка", "Уборка это долго",
                TaskStatus.NEW));
        inMemoryTaskManager.addSubtask(new Subtask("Протереть пыль", "Протирай хорошо",
                TaskStatus.NEW, cleaning.getId()));
        inMemoryTaskManager.addSubtask(new Subtask("Помыть раковину", "Раковину надо мыть " +
                "с порошком",TaskStatus.NEW, cleaning.getId()));
        inMemoryTaskManager.getAllTasks();
        inMemoryTaskManager.getAllEpics();
        inMemoryTaskManager.getAllSubtasks(3);
    }
}