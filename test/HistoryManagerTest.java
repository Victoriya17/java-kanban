import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.HistoryManager;
import com.yandex.app.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void BeforeEach() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Задача", TaskStatus.NEW, "Описание задачи", 90,
                LocalDateTime.of(2025, 11, 1, 9, 0));
        task1.setId(1);
        task2 = new Task("Задача 2", TaskStatus.IN_PROGRESS, "Описание 2", 90,
                LocalDateTime.of(2025, 11, 2, 9, 0));
        task2.setId(2);
        task3 = new Task("Задача 3", TaskStatus.DONE, "Описание 3", 90,
                LocalDateTime.of(2025, 11, 3, 9, 0));
        task3.setId(3);
    }

    @Test
    void add_shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void add_shouldUpdateExistingTaskInHistory() {
        historyManager.add(task1);

        task1.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(TaskStatus.IN_PROGRESS, history.get(0).getStatus());
    }

    @Test
    void add_shouldHandleDuplicatesCorrectly() {
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void add_nullTask_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> historyManager.add(null));
    }

    @Test
    void getHistory_shouldReturnEmptyListWhenNoTasks() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void getHistory_shouldPreserveOrderOfAddition() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void remove_shouldRemoveTaskFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);


        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void remove_shouldRemoveTaskFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);


        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void remove_shouldRemoveTaskFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void remove_nonExistingId_shouldDoNothing() {
        historyManager.add(task1);
        historyManager.remove(999);


        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }


    @Test
    void remove_fromEmptyHistory_shouldDoNothing() {
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}