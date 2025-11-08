import com.yandex.app.model.*;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void testUpdateEpicStatusAllNew() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description",
                TaskStatus.NEW, 10, LocalDateTime.of(2025, 11, 2, 15, 20),
                epicId));

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusAllDone() {
        subtask.setStatus(TaskStatus.DONE);
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description",
                TaskStatus.DONE, 10, LocalDateTime.of(2025, 11, 2, 15, 20),
                epicId));

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusNewAndDone() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description",
                TaskStatus.DONE, 10, LocalDateTime.of(2025, 11, 2, 15, 20),
                epicId));

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusInProgress() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description",
                TaskStatus.IN_PROGRESS, 10, LocalDateTime.of(2025, 11, 2, 15,
                20), epicId));

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testGetHistory() {
        List<Task> history = taskManager.getHistoryManager().getHistory();
        assertEquals(3, history.size());
        assertNotNull(history.get(0));
        assertNotNull(history.get(1));
        assertNotNull(history.get(2));
    }

    @Test
    void testOverLappingInside() {
        Task task2 = new Task("Задача", "Описание", TaskStatus.NEW, 10,
                LocalDateTime.of(2025, 11, 1, 9, 10));
        task2.setId(4);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(task2)
        );

        assertTrue(exception.getMessage().contains("пересекается по времени"),
                "Сообщение должно содержать 'пересекается по времени'");
    }

    @Test
    void testOverLappingBeside() {
        Task task2 = new Task("Задача", "Описание", TaskStatus.NEW, 10,
                LocalDateTime.of(2025, 11, 1, 8, 50));
        task2.setId(4);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(task2)
        );

        assertTrue(exception.getMessage().contains("пересекается по времени"),
                "Сообщение должно содержать 'пересекается по времени'");
    }

    @Test
    void testOverLappingBorder() {
        Task task2 = new Task("Задача", "Описание", TaskStatus.NEW, 50,
                LocalDateTime.of(2025, 11, 1, 8, 50));
        task2.setId(4);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(task2)
        );

        assertTrue(exception.getMessage().contains("пересекается по времени"),
                "Сообщение должно содержать 'пересекается по времени'");
    }
}