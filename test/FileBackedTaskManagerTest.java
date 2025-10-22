import com.yandex.app.model.*;
import com.yandex.app.service.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager fileBackedTaskManager;
    private Task task;
    private int taskId;
    private Task savedTask;
    private Subtask subtask;
    private int subtaskId;
    private Subtask savedSubtask;
    private Epic epic;
    private int epicId;
    private Epic savedEpic;
    private static File file;

    @BeforeEach
    void beforeEach() throws IOException {
        file = File.createTempFile("test", "csv");
        fileBackedTaskManager = new FileBackedTaskManager(file);
        task = fileBackedTaskManager.addTask(new Task(1, TypeOfTasks.TASK, "Задача", TaskStatus.NEW,
                "Описание задачи"));
        assertNotNull(task, "Задача не была создана.");
        taskId = task.getId();
        savedTask = fileBackedTaskManager.getTaskById(taskId);
        epic = fileBackedTaskManager.addEpic(new Epic(2, TypeOfTasks.EPIC,"name", TaskStatus.NEW,
                "description"));
        epicId = epic.getId();
        savedEpic = fileBackedTaskManager.getEpicById(epicId);
        subtask = fileBackedTaskManager.addSubtask(new Subtask(3, TypeOfTasks.SUBTASK,"subtask",
                TaskStatus.NEW, "description",  epicId));
        subtaskId = subtask.getId();
        savedSubtask = fileBackedTaskManager.getSubtaskById(subtaskId);
        fileBackedTaskManager.save();
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
        final List<Task> tasks = fileBackedTaskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
    }

    @Test
    void subtasksListNotNull() {
        final List<Subtask> subtasks = fileBackedTaskManager.getAllSubtasks(epicId);
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
    }

    @Test
    void epicsListNotNull() {
        final List<Epic> epics = fileBackedTaskManager.getAllEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
    }

    @Test
    void oneTaskInTasksList() {
        final List<Task> tasks = fileBackedTaskManager.getAllTasks();
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void oneSubtaskInSubtasksList() {
        final List<Subtask> subtasks = fileBackedTaskManager.getAllSubtasks(epicId);
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    void oneEpicInEpicsList() {
        final List<Epic> epics = fileBackedTaskManager.getAllEpics();
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void tasksEquals() {
        final List<Task> tasks = fileBackedTaskManager.getAllTasks();
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void subtasksEquals() {
        final List<Subtask> subtasks = fileBackedTaskManager.getAllSubtasks(epicId);
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void deleteTasks() {
        fileBackedTaskManager.deleteAllTasks();
        final List<Task> tasks = fileBackedTaskManager.getAllTasks();
        assertEquals(0, tasks.size(), "Список задач должен быть пуст после удаления всех задач.");
    }

    @Test
    void deleteSubtasks() {
        fileBackedTaskManager.deleteAllSubtasks();
        final List<Subtask> subtasks = fileBackedTaskManager.getAllSubtasks(epicId);
        assertEquals(0, subtasks.size(), "Список подзадач должен быть пуст после удаления всех" +
                " подзадач.");
    }

    @Test
    void deleteEpics() {
        fileBackedTaskManager.deleteAllEpics();
        final List<Epic> epics = fileBackedTaskManager.getAllEpics();
        assertEquals(0, epics.size(), "Список эпиков должен быть пуст после удаления всех эпиков.");
    }

    @Test
    void idUnique() {
        Task task2 = fileBackedTaskManager.addTask(new Task(4, TypeOfTasks.TASK,"Task2",
                TaskStatus.IN_PROGRESS, "description2"));
        Epic epic2 = fileBackedTaskManager.addEpic(new Epic(5, TypeOfTasks.EPIC, "name2", TaskStatus.NEW,
                "description2"));
        epicId = epic2.getId();
        Subtask subtask2 = fileBackedTaskManager.addSubtask(new Subtask(6, TypeOfTasks.SUBTASK, "subtask",
                TaskStatus.NEW, "description2", epicId));
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
        List<Task> history = fileBackedTaskManager.historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(savedTask, history.get(0));
        assertEquals(savedEpic, history.get(1));
        assertEquals(savedSubtask, history.get(2));
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks(epic.getId()).isEmpty());
    }

    @Test
    void testSaveAndLoadMultipleTasks() throws IOException {
        fileBackedTaskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loadedManager.getTaskById(task.getId()));
        assertNotNull(loadedManager.getEpicById(epic.getId()));
        assertNotNull(loadedManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testLoadAllTasks() throws IOException {
        fileBackedTaskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> allTasks = loadedManager.getAllTasks();
        assertEquals(1, allTasks.size());
    }
}