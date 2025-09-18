import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;
    private Task task;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("name", "description", 1, TaskStatus.NEW);
        historyManager.add(task);
    }

    @Test
    void add() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        Task savedTask = (Task) history.get(0);
        assertEquals("name", savedTask.getNameOfTask());
        assertEquals("description", savedTask.getDescriptionOfTask());
        assertEquals(1, savedTask.getId());
        assertEquals(TaskStatus.NEW, savedTask.getStatus());

        task.setStatus(TaskStatus.DONE);
        historyManager.add(task);
        assertEquals(TaskStatus.NEW, savedTask.getStatus());
        assertEquals(2, history.size(), "При обновлении задачи, в истории должны быть обе задачи");
    }

    @Test
    void saveOldTask() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        Task task2 = new Task("name2", "description2", 1, TaskStatus.NEW);
        historyManager.add(task2);
        assertEquals(2, history.size(), "При добавлении следующей задачи, старая остаётся");
    }

   @Test
   void checkHistory() {

   }
}