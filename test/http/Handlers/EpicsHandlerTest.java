package http.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapters.DurationAdapter;
import com.yandex.app.http.adapters.LocalDateTimeAdapter;
import com.yandex.app.model.Epic;
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

class EpicsHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public EpicsHandlerTest() throws IOException {
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
        URI url = URI.create("http://localhost:8080/task/2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertEquals(0, tasksFromManager.size(), "Некорректное количество эпиков");
    }

    @Test
    void testMissingMethod() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Testing epic 1", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .HEAD()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
    }

    @Test
    void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Testing epic 1", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type epicType = new TypeToken<List<Epic>>() {
        }.getType();

        List<Epic> epicsList = gson.fromJson(response.body(), epicType);

        assertNotNull(epicsList, "Эпики не возвращаются");
        assertEquals(1, epicsList.size(), "Некорректное количество эпиков");
    }

    @Test
    void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test 2", tasksFromManager.get(0).getNameOfTask(), "Некорректное имя эпиков");
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Test 1", "Testing epic 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addEpic(epic1);

        Epic epic2 = new Epic("Test 2", "Testing epic 2", epic1.getStatus(),
                epic1.getDuration().toMinutes(), LocalDateTime.of(2025, 7, 2, 9, 0));
        epic2.setId(epic1.getId());
        String epicJson = gson.toJson(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(epicJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test 2", tasksFromManager.getFirst().getNameOfTask(), "Некорректное имя эпика");
    }

    @Test
    void testDeleteAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        epic.setId(1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        manager.deleteAllEpics();
        List<Epic> tasksFromManager = manager.getAllEpics();

        assertEquals(0, tasksFromManager.size(), "Некорректное количество эпиков");
        assertNotNull(tasksFromManager, "Эпики не возвращаются");
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic tasksFromManager = gson.fromJson(response.body(), Epic.class);

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(epic.getNameOfTask(), tasksFromManager.getNameOfTask(), "Некорректное имя эпика 1");

    }

    @Test
    void testDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicList = manager.getAllEpics();

        assertNotNull(epicList, "Эпики не возвращаются");
        assertEquals(0, epicList.size(), "Некорректное количество эпиков");
    }

    @Test
    void testGetEpicsFromEmptyMap() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался пустой список эпиков");
    }

    @Test
    void testDeleteEpicWithIncorrectId() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW, 90,
                LocalDateTime.now());
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/incorrect");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        List<Epic> epicList = manager.getAllEpics();

        assertNotNull(epicList, "Эпики не возвращаются");
        assertEquals(1, epicList.size(), "Задано некорректное id");
    }
}