import com.yandex.app.model.*;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yandex.app.service.InMemoryTaskManager.getNextId;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;
    private Task task;
    private int taskId;
    private Task savedTask;
    private Subtask subtask;
    private int subtaskId;
    private Subtask savedSubtask;
    private Epic epic;
    private int epicId;
    private Epic savedEpic;

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
        task = taskManager.addTask(new Task("Задача", "Описание задачи", 1,
                TaskStatus.NEW));
        assertNotNull(task, "Задача не была создана.");
        taskId = task.getId();
        savedTask = taskManager.getTaskById(taskId);
        epic = taskManager.addEpic(new Epic("name", "description", 2,
                TaskStatus.NEW));
        epicId = epic.getId();
        savedEpic = taskManager.getEpicById(epicId);
        subtask = taskManager.addSubtask(new Subtask("subtask", "description", 3,
                TaskStatus.NEW, epicId));
        subtaskId = subtask.getId();
        savedSubtask = taskManager.getSubtaskById(subtaskId);
    }

    @Test
    void taskNotNull() {
        assertNotNull(savedTask, "Задача не найдена.");
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
        Task task2 = taskManager.addTask(new Task("Task2", "description2", getNextId(),
                TaskStatus.IN_PROGRESS));
        Epic epic2 = taskManager.addEpic(new Epic("name2", "description2", getNextId(),
                TaskStatus.NEW));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description2",
                getNextId(), TaskStatus.NEW, epic2.getId()));
        assertNotEquals(task.getId(), task2.getId());
        assertNotEquals(task.getId(), subtask.getId());
        assertNotEquals(task.getId(), epic.getId());
        assertNotEquals(subtask.getId(), subtask2.getId());
        assertNotEquals(epic.getId(), epic2.getId());
    }

    @Test
    void checkImmutability() {
        task.setNameOfTask("name");
        assertEquals("Задача", savedTask.getNameOfTask());
        assertEquals("name", task.getNameOfTask());
    }

    @Test
    void testGetHistory() {
        List<Task> history = taskManager.historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(savedTask, history.get(0));
        assertEquals(savedEpic, history.get(1));
        assertEquals(savedSubtask, history.get(2));
    }
}