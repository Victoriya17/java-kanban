package http.Handlers;

import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrioritizedHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    public PrioritizedHandlerTest() throws IOException {
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
    void testPrioritizedHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 5, 2, 9, 0));
        manager.addTask(task1);

        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 7, 2, 9, 0));
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> historyFromManager = manager.getPrioritizedTasks();

        assertNotNull(historyFromManager, "Задачи не возвращаются");
        assertEquals(2, historyFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", historyFromManager.getFirst().getNameOfTask(), "Некорректное имя " +
                "задачи");
    }

    @Test
    void testGetPrioritizedFromEmptyMap() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> historyFromManager = manager.getPrioritizedTasks();

        assertNotNull(historyFromManager, "Задачи не возвращаются");
        assertEquals(0, historyFromManager.size(), "Некорректное количество задач");
    }
}