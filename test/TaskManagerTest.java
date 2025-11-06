import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected abstract T createTaskManager();

    protected T taskManager;

    protected Task task;
    private int taskId;
    private Task savedTask;
    protected Subtask subtask;
    private int subtaskId;
    private Subtask savedSubtask;
    protected Epic epic;
    protected int epicId;
    private Epic savedEpic;

    @BeforeEach
    void beforeEach() {
        taskManager = createTaskManager();
        task = taskManager.addTask(new Task("Задача", TaskStatus.NEW,
                "Описание задачи", 90, LocalDateTime.of(2025, 11, 01,
                9, 0)));
        assertNotNull(task, "Задача не была создана.");
        taskId = task.getId();
        savedTask = taskManager.getTaskById(taskId);
        epic = taskManager.addEpic(new Epic("name", TaskStatus.NEW, "description",
                60, LocalDateTime.of(2025, 11, 1, 14, 0)));
        epicId = epic.getId();
        savedEpic = taskManager.getEpicById(epicId);
        subtask = taskManager.addSubtask(new Subtask("subtask", TaskStatus.NEW, "description",
                10, LocalDateTime.of(2025, 11, 1, 15, 10), epicId));
        subtaskId = subtask.getId();
        savedSubtask = taskManager.getSubtaskById(subtaskId);
    }

    @Test
    void taskNotNull() {
        assertNotNull(savedTask, "Задача не была создана.");
    }

    @Test
    void subtaskNotNull() {
        assertNotNull(savedSubtask, "Подзадача не найдена.");
    }

    @Test
    void epicNotNull() {
        assertNotNull(savedEpic, "Эпик не найден.");
    }

    @Test
    void tasksListNotNull() {
        final List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
    }

    @Test
    void subtasksListNotNull() {
        final List<Subtask> subtasks = taskManager.getAllSubtasks(epicId);
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
    }

    @Test
    void epicsListNotNull() {
        final List<Epic> epics = taskManager.getAllEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
    }


    @Test
    void deleteTasks() {
        taskManager.deleteAllTasks();
        final List<Task> tasks = taskManager.getAllTasks();
        assertEquals(0, tasks.size(), "Список задач должен быть пуст после удаления всех задач.");
    }

    @Test
    void deleteSubtasks() {
        taskManager.deleteAllSubtasks();
        final List<Subtask> subtasks = taskManager.getAllSubtasks(epicId);
        assertEquals(0, subtasks.size(), "Список подзадач должен быть пуст после удаления всех" +
                " подзадач.");
    }

    @Test
    void deleteEpics() {
        taskManager.deleteAllEpics();
        final List<Epic> epics = taskManager.getAllEpics();
        assertEquals(0, epics.size(), "Список эпиков должен быть пуст после удаления всех эпиков.");
    }

    @Test
    void idUnique() {
        Task task2 = taskManager.addTask(new Task("Task2", "description2",
                TaskStatus.IN_PROGRESS));
        Epic epic2 = taskManager.addEpic(new Epic("name2", "description2",
                TaskStatus.NEW));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description2",
                TaskStatus.NEW, epic2.getId()));
        assertNotEquals(task.getId(), task2.getId());
        assertNotEquals(task.getId(), subtask.getId());
        assertNotEquals(task.getId(), epic.getId());
        assertNotEquals(subtask.getId(), subtask2.getId());
        assertNotEquals(epic.getId(), epic2.getId());
    }

    @Test
    void tasksEqualsById() {
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void subtasksEqualsById() {
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
    }

    @Test
    void epicsEqualsById() {
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void oneTaskInTasksList() {
        final List<Task> tasks = taskManager.getAllTasks();
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void oneSubtaskInSubtasksList() {
        final List<Subtask> subtasks = taskManager.getAllSubtasks(epicId);
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    void oneEpicInEpicsList() {
        final List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void tasksEquals() {
        final List<Task> tasks = taskManager.getAllTasks();
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void subtasksEquals() {
        final List<Subtask> subtasks = taskManager.getAllSubtasks(epicId);
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void checkImmutability() {
        task.setNameOfTask("name");
        assertEquals("Задача", savedTask.getNameOfTask());
        assertEquals("name", task.getNameOfTask());
    }

    @Test
    void testSubtaskHasEpic() {
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", TaskStatus.IN_PROGRESS,
                "description", 10, LocalDateTime.of(2025, 12, 1,
                15, 20), epicId));
        Epic epic2 = taskManager.getEpicById(subtask2.getEpicId());
        assertNotNull(epic2, "Подзадача должна быть связана с эпиком.");
    }
}