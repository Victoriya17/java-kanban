import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Node<Task> head = historyManager.head;
        assertNotNull(head, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, historyManager.getHistory().size(), "После добавления задачи, история не " +
                "должна быть пустой.");

        Task savedTask = head.task;
        assertEquals("name", savedTask.getNameOfTask());
        assertEquals("description", savedTask.getDescriptionOfTask());
        assertEquals(1, savedTask.getId());
        assertEquals(TaskStatus.NEW, savedTask.getStatus());

        task.setStatus(TaskStatus.DONE);
        historyManager.add(task);
        assertEquals(TaskStatus.DONE, savedTask.getStatus());
        assertEquals(1, historyManager.getHistory().size(), "При обновлении задачи с тем же ID, " +
                "размер истории не должен увеличиваться");
    }

    @Test
    void saveNewTask() {
        assertEquals(1, historyManager.getHistory().size(), "После добавления задачи, история не " +
                "должна быть пустой.");
        Task task2 = new Task("name2", "description2", 1, TaskStatus.NEW);
        historyManager.add(task2);

        assertEquals(1, historyManager.getHistory().size(), "При добавлении следующей задачи с тем" +
                "же ID, размер истории не изменяется");
    }

    @Test
    void deleteTask() {
        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size(), "После удаления задачи, история должна " +
                "быть пуста");
    }

    @Test
    void removedTasksDoNotKeepOldIds() {
        assertTrue(historyManager.getHistory().contains(task));
        historyManager.remove(task.getId());
        assertFalse(historyManager.getHistory().contains(task));
        Task newTask = new Task("newName", "newDescription", 1, TaskStatus.NEW);
        historyManager.add(newTask);
        assertTrue(historyManager.getHistory().contains(newTask));
    }

    @Test
    void removedSubtaskDoNotKeepOldIds() {
        Epic epic = new Epic("nameEpic", "descriptionEpic", 2, TaskStatus.NEW);
        historyManager.add(epic);
        Subtask subtask = new Subtask("name", "description", 3, TaskStatus.NEW, epic);
        historyManager.add(subtask);
        assertTrue(historyManager.getHistory().contains(subtask));

        historyManager.remove(subtask.getId());
        assertFalse(historyManager.getHistory().contains(subtask), "Подзадача должна быть удалена из эпика");

        Subtask newSubtask = new Subtask("newName", "newDescription", 3, TaskStatus.NEW,
                epic);
        historyManager.add(newSubtask);
        assertTrue(historyManager.getHistory().contains(newSubtask));
    }

    @Test
    void changeFieldsBySetters() {
        Epic epic = new Epic("nameEpic", "descriptionEpic", 2, TaskStatus.NEW);
        historyManager.add(epic);
        Subtask subtask = new Subtask("name", "description", 3, TaskStatus.NEW, epic);
        historyManager.add(subtask);

        assertEquals("name", subtask.getNameOfTask());
        assertEquals(TaskStatus.NEW, subtask.getStatus());

        subtask.setNameOfTask("newName");
        subtask.setStatus(TaskStatus.DONE);

        historyManager.add(subtask);
        assertNotNull(subtask);
        assertEquals("newName", subtask.getNameOfTask());
        assertEquals(TaskStatus.DONE, subtask.getStatus());
    }
}