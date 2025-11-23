package com.yandex.app.http.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.exceptions.TimeOverlapException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.exceptions.ManagerSaveException;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем GET запрос и выводим список всех задач");
        List<Subtask> subtasksList = taskManager.getAllSubtasks();
        if (subtasksList.isEmpty()) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(subtasksList), 200);
    }

    private void handlePostSubtasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем POST запрос");
        try {
            JsonObject jsonObject = getJsonFromRequestBody(exchange);
            Subtask subtask = gson.fromJson(jsonObject, Subtask.class);
            if (subtask.getId() != 0) {
                System.out.println("Обновляем подзадачу N " + subtask.getId());
                taskManager.updateSubtask(subtask);
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                System.out.println("Создаем задачу " + subtask.getNameOfTask());
                Subtask savedSubtask = taskManager.addSubtask(subtask);
                sendText(exchange, gson.toJson(savedSubtask), 201);
            }
        } catch (TimeOverlapException e) {
            System.out.println("Задачи пересекаются " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка обновления подзадачи " + e.getMessage());
            sendBadRequest(exchange);
        } catch (ManagerSaveException | IOException exception) {
            sendInternalServerError(exchange);
            System.out.println(exception.getMessage());
        } catch (Exception e) {
            System.out.println("Внутренняя ошибка при обновлении подзадачи " + e.getMessage());
            sendInternalServerError(exchange);
        }
    }

    private void handleDeleteSubtasks(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем DELETE запрос, удаляем все задачи");
        taskManager.deleteAllSubtasks();
        boolean allDeleted = true;

        List<Integer> epicIds = taskManager.getAllEpics().stream()
                .map(Epic::getId)
                .toList();

        for (Integer epicId : epicIds) {
            List<Subtask> subtasksOfEpic = taskManager.getEpicSubtasks(epicId);
            if (!subtasksOfEpic.isEmpty()) {
                allDeleted = false;
                break;
            }
        }

        if (allDeleted) {
            sendText(exchange, "{\"message\": \"Все подзадачи успешно удалены\"}", 200);
        } else {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetSubtaskById(HttpExchange exchange, Optional<Integer> subtaskId) throws IOException {
        System.out.println("Выполняем GET запрос, выводим подзадачу по id");
        if (subtaskId.isPresent()) {
            Subtask subtask = taskManager.getSubtaskById(subtaskId.get());
            if (subtask != null) {
                System.out.println("Подзадача по id " + subtaskId.get() + " найдена");
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                System.out.println("Подзадача не найдена");
                sendNotFound(exchange);
            }
        }
    }

    private void handleDeleteSubtaskById(HttpExchange exchange, Optional<Integer> subtaskId) throws IOException {
        System.out.println("Выполняем DELETE запрос");
        if (subtaskId.isEmpty()) {
            System.out.println("Ошибка: ID подзадачи не указан");
            sendBadRequest(exchange);
            return;
        }

        int id = subtaskId.get();

        try {
            taskManager.deleteSubtaskById(id);

            System.out.println("Подзадача с ID " + id + " успешно удалена");
            sendText(exchange, "{\"message\": \"Подзадача удалена\"}", 200);

        } catch (Exception e) {
            System.out.println("Ошибка при удалении подзадачи с ID " + id + ": " + e.getMessage());
            sendInternalServerError(exchange);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetAllSubtasks(exchange);
                case "POST" -> handlePostSubtasks(exchange);
                case "DELETE" -> handleDeleteSubtasks(exchange);
                default -> sendBadRequest(exchange);
            }
        } else if (splitStrings.length == 3) {
            Optional<Integer> subtaskId = getIdFromPath(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetSubtaskById(exchange, subtaskId);
                case "DELETE" -> handleDeleteSubtaskById(exchange, subtaskId);
                default -> sendBadRequest(exchange);
            }
        }
    }
}