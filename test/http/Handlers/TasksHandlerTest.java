package http.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapters.DurationAdapter;
import com.yandex.app.http.adapters.LocalDateTimeAdapter;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public TasksHandlerTest() throws IOException {
    }

    @BeforeEach
    void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testWrongPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epic/3");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    void testMissingMethod() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .HEAD()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<List<Task>>() {
        }.getType();

        List<Task> tasksList = gson.fromJson(response.body(), taskType);

        assertNotNull(tasksList, "Задачи не возвращаются");
        assertEquals(1, tasksList.size(), "Некорректное количество задач");
    }

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getNameOfTask(), "Некорректное имя задачи");
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addTask(task1);

        Task task2 = new Task("Test 2", "Testing task 2", task1.getStatus(),
                task1.getDuration().toMinutes(), LocalDateTime.of(2025, 7, 2, 9, 0));
        task2.setId(task1.getId());
        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(taskJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getNameOfTask(), "Некорректное имя задачи");
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        task.setId(1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        manager.deleteAllTasks();
        List<Task> tasksFromManager = manager.getAllTasks();

        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task tasksFromManager = gson.fromJson(response.body(), Task.class);

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(task.getNameOfTask(), tasksFromManager.getNameOfTask(), "Некорректное имя задачи 1");

    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> taskList = manager.getAllTasks();

        assertNotNull(taskList, "Задачи не возвращаются");
        assertEquals(0, taskList.size(), "Некорректное количество задач");
    }

    @Test
    void testGetTasksFromEmptyMap() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался пустой список задач");
    }

    @Test
    void testDeleteTaskWithIncorrectId() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/incorrect");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        List<Task> taskList = manager.getAllTasks();

        assertNotNull(taskList, "Задачи не возвращаются");
        assertEquals(1, taskList.size(), "Задано некорректное id");
    }

    @Test
    void testAddTaskWithCrossStartTime() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addTask(task1);

        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW,
                20, LocalDateTime.of(2025, 5, 2, 9, 0));
        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    void testUpdateTaskWithCrossStartTime() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addTask(task1);

        Task task2 = new Task("Test 2", "Testing task 2", task1.getStatus(),
                task1.getDuration().toMinutes(), task1.getStartTime());
        task2.setId(task1.getId());
        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(taskJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }
}