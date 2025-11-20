package com.yandex.app.http.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.ManagerSaveException;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем GET запрос и выводим список всех задач");
        List<Task> tasksList = taskManager.getAllTasks();
        if (tasksList.isEmpty()) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(tasksList), 200);
    }

    private void handleAddTask(HttpExchange exchange, Task task) throws IOException {
        try {
            System.out.println("Создаем задачу " + task.getNameOfTask());
            task.setStatus(TaskStatus.NEW);
            Task savedTask = taskManager.addTask(task);

            if (savedTask == null) {
                sendBadRequest(exchange);
                return;
            }

            sendText(exchange, gson.toJson(savedTask), 201);
        } catch (ManagerSaveException | IOException exception) {
            sendHasInteractions(exchange);
            System.out.println(exception.getMessage());
        } catch (Exception e) {
            sendIternalServerError(exchange);
        }
    }

    private void handleUpdateTask(HttpExchange exchange, Task task) throws IOException {
        try {
            taskManager.updateTask(task);
            System.out.println("Обновляем задачу N " + task.getId());
            sendText(exchange, gson.toJson(task), 200);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка обновления задачи N " + task.getId() + ": " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (Exception e) {
            System.out.println("Внутренняя ошибка при обновлении задачи N " + task.getId() + ": " + e.getMessage());
            sendIternalServerError(exchange);
        }
    }

    private void handlePostTasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем POST запрос");
        JsonObject jsonObject = getJsonFromRequestBody(exchange);
        System.out.println("Тело запроса: " + jsonObject);

        Task task = gson.fromJson(jsonObject, Task.class);

        if (task.getId() != 0) {
            System.out.println("Обновляем задачу с ID: " + task.getId());
            handleUpdateTask(exchange, task);
        } else {
            System.out.println("Создаём новую задачу");
            handleAddTask(exchange, task);
        }
    }

    private void handleDeleteTasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем DELETE запрос, удаляем все задачи");
        taskManager.deleteAllTasks();
        boolean allDeleted = taskManager.getAllTasks().isEmpty();
        if (allDeleted) {
            sendText(exchange, "{\"message\": \"Все задачи успешно удалены\"}", 200);
        } else {
            sendIternalServerError(exchange);
        }
    }

    private void handleGetTaskById(HttpExchange exchange, Optional<Integer> taskId) throws IOException {
        System.out.println("Выполняем GET запрос, выводим Задачу по id");
        if (taskId.isPresent()) {
            Task task = taskManager.getTaskById(taskId.get());
            if (task != null) {
                System.out.println("Задача по id " + taskId.get() + " найдена");
                sendText(exchange, gson.toJson(task), 200);
            } else {
                System.out.println("Задача не найдена");
                sendNotFound(exchange);
            }
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange, Optional<Integer> taskId) throws IOException {
        System.out.println("Выполняем DELETE запрос");
        if (taskId.isEmpty()) {
            System.out.println("Ошибка: ID задачи не указан");
            sendBadRequest(exchange);
            return;
        }

        taskManager.deleteTaskById(taskId.get());
        Task deleted = taskManager.getTaskById(taskId.get());

        if (deleted == null) {
            System.out.println("Задача с ID " + taskId.get() + " успешно удалена");
            sendText(exchange, "{\"message\": \"Задача удалена\"}", 200);
        } else {
            System.out.println("Задача не удалена");
            sendIternalServerError(exchange);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetAllTasks(exchange);
                case "POST" -> handlePostTasks(exchange);
                case "DELETE" -> handleDeleteTasks(exchange);
                default -> sendBadRequest(exchange);
            }
        } else if (splitStrings.length == 3) {
            Optional<Integer> taskId = getIdFromPath(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetTaskById(exchange, taskId);
                case "DELETE" -> handleDeleteTaskById(exchange, taskId);
                default -> sendBadRequest(exchange);
            }
        }
    }
}