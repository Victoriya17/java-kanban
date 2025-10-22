package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task newTask = inMemoryTaskManager.addTask(new Task("Почистить фильтр", "Фильтр " +
                "бризера надо чистить 2 раза в месяц", 1, TaskStatus.NEW));
        inMemoryTaskManager.getTaskById(newTask.getId());
        inMemoryTaskManager.addTask(new Task("Помыть полы", "Мыть полы надо 4 раза в месяц",
                2, TaskStatus.NEW));
        Epic cleaning = inMemoryTaskManager.addEpic(new Epic("Уборка", "Уборка это долго",
                3, TaskStatus.NEW));
        inMemoryTaskManager.addSubtask(new Subtask("Протереть пыль", "Протирай хорошо", 4,
                TaskStatus.NEW, cleaning.getId()));
        inMemoryTaskManager.addSubtask(new Subtask("Помыть раковину", "Раковину надо мыть " +
                "с порошком", 5, TaskStatus.NEW, cleaning.getId()));
        inMemoryTaskManager.getAllTasks();
        inMemoryTaskManager.getAllEpics();
        inMemoryTaskManager.getAllSubtasks(3);
        inMemoryTaskManager.updateSubtaskStatus(4, TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubtaskStatus(5, TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getAllSubtasks(3);
        inMemoryTaskManager.getEpicById(3);
        List<Task> history = inMemoryTaskManager.historyManager.getHistory();
        for (Object histories : history) {
            System.out.println(histories);
        }
    }
}