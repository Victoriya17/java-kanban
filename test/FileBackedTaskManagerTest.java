import com.yandex.app.model.*;
import com.yandex.app.service.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager fileBackedTaskManager;
    private Task task;
    private Subtask subtask;
    private Epic epic;
    private int epicId;
    private static File file;

    @BeforeEach
    void beforeEach() throws IOException {
        file = File.createTempFile("test", "csv");
        fileBackedTaskManager = new FileBackedTaskManager(file);
        task = fileBackedTaskManager.addTask(new Task(1, TypeOfTasks.TASK, "Задача", TaskStatus.NEW,
                "Описание задачи"));
        assertNotNull(task, "Задача не была создана.");
        epic = fileBackedTaskManager.addEpic(new Epic(2, TypeOfTasks.EPIC, "name", TaskStatus.NEW,
                "description"));
        epicId = epic.getId();
        subtask = fileBackedTaskManager.addSubtask(new Subtask(3, TypeOfTasks.SUBTASK, "subtask",
                TaskStatus.NEW, "description", epicId));
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        fileBackedTaskManager.deleteAllTasks();
        fileBackedTaskManager.deleteAllEpics();
        fileBackedTaskManager.deleteAllSubtasks();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks(epic.getId()).isEmpty());

        String fileContent = new String(Files.readAllBytes(file.toPath()));
        String[] lines = fileContent.split("\n");
        assertEquals(1, lines.length, "Файл должен содержать ровно одну строку.");
    }

    @Test
    void testSaveAndLoadMultipleTasks() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(fileBackedTaskManager.getAllTasks(), loadedManager.getAllTasks());
        assertEquals(fileBackedTaskManager.getAllSubtasks(2), loadedManager.getAllSubtasks(2));
        assertEquals(fileBackedTaskManager.getAllEpics(), loadedManager.getAllEpics());
    }

    @Test
    void testLoadAllTasks() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> allTasks = loadedManager.getAllTasks();
        assertEquals(1, allTasks.size());
    }
}