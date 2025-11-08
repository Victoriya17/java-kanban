import com.yandex.app.model.*;
import com.yandex.app.service.FileBackedTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static File file;

    static {
        try {
            file = File.createTempFile("test", "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(file);
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        List<Epic> epics = loadedManager.getAllEpics();
        if (!epics.isEmpty()) {
            for (Epic epic : epics) {
                assertTrue(loadedManager.getAllSubtasks(epic.getId()).isEmpty(),
                        "Подзадачи для эпика " + epic.getId() + " должны отсутствовать");
            }
        }

        String fileContent = new String(Files.readAllBytes(file.toPath()));
        String[] lines = fileContent.split("\n");
        assertEquals(1, lines.length, "Файл должен содержать ровно одну строку.");
    }

    @Test
    void testSaveAndLoadMultipleTasks() throws IOException {

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(taskManager.getAllTasks(), loadedManager.getAllTasks());
        assertEquals(taskManager.getAllSubtasks(2), loadedManager.getAllSubtasks(2));
        assertEquals(taskManager.getAllEpics(), loadedManager.getAllEpics());
    }

    @Test
    void testLoadAllTasks() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> allTasks = loadedManager.getAllTasks();
        assertEquals(1, allTasks.size(), "Должна быть ровно одна задача");
        assertEquals(task.getId(), allTasks.get(0).getId(), "ID задачи должен совпадать");
        assertEquals(task.getNameOfTask(), allTasks.get(0).getNameOfTask(),
                "Название задачи должно совпадать");
    }

    @Test
    void testExceptionWhenFileNotFound() {
        File nonExistentFile = new File("non-existent-file.csv");
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        });
        assertEquals("Файл не найден: " + nonExistentFile.getPath(), exception.getMessage());
    }

    @Test
    void testNoExceptionWhenFileExistsAndIsEmpty() throws IOException {
        try {
            Files.write(file.toPath(), "".getBytes());
            Assertions.assertDoesNotThrow(() -> {
                FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
            });
        } finally {
            file.delete();
        }
    }
}