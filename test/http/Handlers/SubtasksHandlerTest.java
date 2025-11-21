package http.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yandex.app.exceptions.TimeOverlapException;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapters.DurationAdapter;
import com.yandex.app.http.adapters.LocalDateTimeAdapter;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubtasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    int epicId;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public SubtasksHandlerTest() throws IOException {
    }

    @BeforeEach
    void setUp() throws TimeOverlapException {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        Epic epic = new Epic("Test 1", "Testing epic 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addEpic(epic);
        epicId = epic.getId();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testWrongPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/task/3");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertEquals(0, tasksFromManager.size(), "Некорректное количество подзадач");
    }

    @Test
    void testMissingMethod() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .HEAD()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
    }

    @Test
    void testGetSubtasks() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type subtaskType = new TypeToken<List<Subtask>>() {
        }.getType();

        List<Subtask> subtasksList = gson.fromJson(response.body(), subtaskType);

        assertNotNull(subtasksList, "Подзадачи не возвращаются");
        assertEquals(1, subtasksList.size(), "Некорректное количество подзадач");
    }

    @Test
    void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 2", tasksFromManager.get(0).getNameOfTask(), "Некорректное имя подзадачи");
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask1 = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW,
                90, LocalDateTime.of(2025, 5, 2, 9, 0), epicId);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Test 2", "Testing subtask 2", subtask1.getStatus(),
                subtask1.getDuration().toMinutes(), LocalDateTime.of(2025, 7, 2, 9,
                0), epicId);
        subtask2.setId(subtask1.getId());
        String subtaskJson = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(subtaskJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 2", tasksFromManager.getFirst().getNameOfTask(), "Некорректное имя " +
                "подзадачи");
    }

    @Test
    void testDeleteAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        subtask.setId(2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        manager.deleteAllSubtasks();
        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertEquals(0, tasksFromManager.size(), "Некорректное количество подзадач");
        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask tasksFromManager = gson.fromJson(response.body(), Subtask.class);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(subtask.getNameOfTask(), tasksFromManager.getNameOfTask(), "Некорректное имя подзадачи");
    }

    @Test
    void testDeleteSubtaskById() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtaskList = manager.getEpicSubtasks(epicId);

        assertNotNull(subtaskList, "Подзадачи не возвращаются");
        assertEquals(0, subtaskList.size(), "Некорректное количество подзадач");
    }

    @Test
    void testGetSubtasksFromEmptyMap() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался пустой список подзадач");
    }

    @Test
    void testDeleteSubtaskWithIncorrectId() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                90, LocalDateTime.now(), epicId);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/incorrect");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        List<Subtask> subtaskList = manager.getEpicSubtasks(epicId);

        assertNotNull(subtaskList, "Подзадачи не возвращаются");
        assertEquals(1, subtaskList.size(), "Задано некорректное id");
    }

    @Test
    void testAddSubtaskWithCrossStartTime() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask1 = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW,
                90, LocalDateTime.of(2025, 5, 2, 9, 0), epicId);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW,
                20, LocalDateTime.of(2025, 5, 2, 9, 0), epicId);
        String subtaskJson = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    void testUpdateSubtaskWithCrossStartTime() throws IOException, InterruptedException, TimeOverlapException {
        Subtask subtask1 = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW,
                90, LocalDateTime.of(2025, 5, 2, 9, 0), epicId);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Test 2", "Testing subtask 2", subtask1.getStatus(),
                subtask1.getDuration().toMinutes(), subtask1.getStartTime(), epicId);
        subtask2.setId(subtask1.getId());
        String subtaskJson = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(subtaskJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());

        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epicId);

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
    }
}