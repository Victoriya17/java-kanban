package com.yandex.app.http.Handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Обрабатываем GET запрос к /history");
        try {
            List<Task> historyList = taskManager.getHistory();
            if (historyList.isEmpty()) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, gson.toJson(historyList), 200);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении истории: " + e.getMessage());
            sendIternalServerError(exchange);
        }
    }
}