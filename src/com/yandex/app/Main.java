package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task newTask = inMemoryTaskManager.addTask("Почистить фильтр", "Фильтр бризера надо чистить 2 раза в месяц", TaskStatus.NEW);
        inMemoryTaskManager.getTaskById(newTask.getId());
        inMemoryTaskManager.addTask("Помыть полы", "Мыть полы надо 4 раза в месяц", TaskStatus.NEW);
        Epic cleaning = inMemoryTaskManager.addEpic("Уборка", "Уборка это долго", TaskStatus.NEW);
        inMemoryTaskManager.addSubtask("Протереть пыль", "Протирай хорошо", TaskStatus.NEW, cleaning);
        inMemoryTaskManager.addSubtask("Помыть раковину", "Раковину надо мыть с порошком", TaskStatus.NEW, cleaning);
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