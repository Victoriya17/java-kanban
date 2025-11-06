import com.yandex.app.model.*;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private Task savedTask;
    private Subtask savedSubtask;
    private Epic savedEpic;

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void testUpdateEpicStatusAllNew() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", TaskStatus.NEW,
                "description", 10, LocalDateTime.of(2025, 11, 2,
                15, 20), epicId));

        taskManager.updateEpicStatus(epicId);
        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusAllDone() {
        taskManager.updateSubtaskStatus(subtask.getId(), TaskStatus.DONE);
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", TaskStatus.DONE,
                "description", 10, LocalDateTime.of(2025, 11, 2,
                15, 20), epicId));

        taskManager.updateEpicStatus(epicId);
        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusNewAndDone() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", TaskStatus.DONE,
                "description", 10, LocalDateTime.of(2025, 11, 2,
                15, 20), epicId));

        taskManager.updateEpicStatus(epicId);
        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpicStatusInProgress() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", TaskStatus.IN_PROGRESS,
                "description", 10, LocalDateTime.of(2025, 11, 2,
                15, 20), epicId));

        taskManager.updateEpicStatus(epicId);
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
        Task task2 = new Task("Задача", TaskStatus.NEW, "Описание", 10,
                LocalDateTime.of(2025, 11, 1, 9, 10));
        task2.setId(4);
        assertTrue(taskManager.hasOverlappingTasks(task2).isPresent());
    }

    @Test
    void testOverLappingBeside() {
        Task task2 = new Task("Задача", TaskStatus.NEW, "Описание", 10,
                LocalDateTime.of(2025, 11, 1, 8, 50));
        task2.setId(4);
        assertTrue(taskManager.hasOverlappingTasks(task2).isPresent());
    }

    @Test
    void testOverLappingBorder() {
        Task task2 = new Task("Задача", TaskStatus.NEW, "Описание", 50,
                LocalDateTime.of(2025, 11, 1, 8, 50));
        task2.setId(4);
        assertTrue(taskManager.hasOverlappingTasks(task2).isPresent());
    }
}