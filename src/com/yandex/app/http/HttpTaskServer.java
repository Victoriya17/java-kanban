package com.yandex.app.http;
import java.io.IOException;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import com.yandex.app.http.adapters.DurationAdapter;
import com.yandex.app.http.adapters.LocalDateTimeAdapter;
import com.yandex.app.service.TaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.http.Handlers.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class HttpTaskServer {
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final int port = 8080;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.taskManager = taskManager;
        httpServer.createContext("/tasks", new TasksHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicsHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.httpServer.start();
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + port + " порту.");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP-сервер на порту " + port + " остановлен.");
    }

    public Gson getGson() {
        return this.gson;
    }
}