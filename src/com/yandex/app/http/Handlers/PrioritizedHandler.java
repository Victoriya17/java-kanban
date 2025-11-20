package com.yandex.app.http.Handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Обрабатываем GET запрос к /prioritized");
        try {
            List<Task> prioritizedList = taskManager.getPrioritizedTasks();
            if (prioritizedList.isEmpty()) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, gson.toJson(prioritizedList), 200);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении списка: " + e.getMessage());
            sendIternalServerError(exchange);
        }
    }
}