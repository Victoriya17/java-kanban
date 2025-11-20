package com.yandex.app.http.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Epic;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.ManagerSaveException;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем GET запрос и выводим список всех эпиков");
        List<Epic> epicsList = taskManager.getAllEpics();
        if (epicsList.isEmpty()) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(epicsList), 200);
    }

    private void handleAddEpic(HttpExchange exchange, Epic epic) throws IOException {
        try {
            System.out.println("Создаем задачу " + epic.getNameOfTask());
            epic.setStatus(TaskStatus.NEW);
            Epic savedEpic = taskManager.addEpic(epic);

            if (savedEpic == null) {
                sendBadRequest(exchange);
                return;
            }

            sendText(exchange, gson.toJson(savedEpic), 201);
        } catch (ManagerSaveException | IOException exception) {
            sendHasInteractions(exchange);
            System.out.println(exception.getMessage());
        } catch (Exception e) {
            sendIternalServerError(exchange);
        }
    }

    private void handleUpdateEpic(HttpExchange exchange, Epic epic) throws IOException {
        try {
            taskManager.updateEpic(epic);
            System.out.println("Обновляем эпик N " + epic.getId());
            sendText(exchange, gson.toJson(epic), 200);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка обновления эпика N " + epic.getId() + ": " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (Exception e) {
            System.out.println("Внутренняя ошибка при обновлении эпика N " + epic.getId() + ": " + e.getMessage());
            sendIternalServerError(exchange);
        }
    }

    private void handlePostEpics(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем POST запрос");
        JsonObject jsonObject = getJsonFromRequestBody(exchange);
        Epic epic = gson.fromJson(jsonObject, Epic.class);
        if (epic.getId() != 0) {
            handleUpdateEpic(exchange, epic);
        } else {
            handleAddEpic(exchange, epic);
        }
    }

    private void handleDeleteEpics(HttpExchange exchange) throws IOException {
        System.out.println("Выполняем DELETE запрос, удаляем все эпики");
        taskManager.deleteAllEpics();
        boolean allDeleted = taskManager.getAllTasks().isEmpty();
        if (allDeleted) {
            sendText(exchange, "{\"message\": \"Все эпики успешно удалены\"}", 200);
        } else {
            sendIternalServerError(exchange);
        }
    }

    private void handleGetEpicById(HttpExchange exchange, Optional<Integer> epicId) throws IOException {
        System.out.println("Выполняем GET запрос, выводим эпик по id");
        if (epicId.isPresent()) {
            Epic epic = taskManager.getEpicById(epicId.get());
            if (epic != null) {
                System.out.println("Эпик по id " + epicId.get() + " найден");
                sendText(exchange, gson.toJson(epic), 200);
            } else {
                System.out.println("Эпик не найден");
                sendNotFound(exchange);
            }
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange, Optional<Integer> epicId) throws IOException {
        System.out.println("Выполняем DELETE запрос");
        if (epicId.isEmpty()) {
            System.out.println("Ошибка: ID эпика не указан");
            sendBadRequest(exchange);
            return;
        }

        taskManager.deleteEpicById(epicId.get());
        System.out.println("Эпик удалён");
        Epic deleted = taskManager.getEpicById(epicId.get());
        if (deleted == null) {
            System.out.println("Задача с ID " + epicId.get() + " успешно удалена");
            sendText(exchange, "{\"message\": \"Эпик удален\"}", 200);
        } else {
            System.out.println("Эпик не найден");
            sendNotFound(exchange);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetAllEpics(exchange);
                case "POST" -> handlePostEpics(exchange);
                case "DELETE" -> handleDeleteEpics(exchange);
                default -> sendBadRequest(exchange);
            }
        } else if (splitStrings.length == 3) {
            Optional<Integer> epicId = getIdFromPath(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGetEpicById(exchange, epicId);
                case "DELETE" -> handleDeleteEpicById(exchange, epicId);
                default -> sendBadRequest(exchange);
            }
        }
    }

}