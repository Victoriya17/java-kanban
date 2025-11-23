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
        task = taskManager.addTask(new Task("Задача", "Описание задачи", TaskStatus.NEW,
                90, LocalDateTime.of(2025, 11, 1, 9, 0)));
        assertNotNull(task, "Задача не была создана.");
        taskId = task.getId();
        savedTask = taskManager.getTaskById(taskId);
        epic = taskManager.addEpic(new Epic("name", "description", TaskStatus.NEW,
                60, LocalDateTime.of(2025, 11, 1, 14, 0)));
        epicId = epic.getId();
        savedEpic = taskManager.getEpicById(epicId);
        subtask = taskManager.addSubtask(new Subtask("subtask", "description", TaskStatus.NEW,
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
        final List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
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
        final List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
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
        Task task2 = taskManager.addTask(new Task("Задача", "Описание задачи",
                TaskStatus.IN_PROGRESS, 90, LocalDateTime.of(2025, 11, 2, 9,
                0)));
        Epic epic2 = taskManager.addEpic(new Epic("name", "description", TaskStatus.NEW,
                60, LocalDateTime.of(2025, 11, 2, 14, 0)));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("subtask", "description",
                TaskStatus.NEW, 10, LocalDateTime.of(2025, 11, 2, 15, 10),
                epicId));
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
        final List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
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
        final List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void testUpdateTask() {
        Task updatedTask = new Task("Новая задача", "Новое описание", TaskStatus.DONE,
                120, LocalDateTime.of(2025, 11, 2, 10, 0));
        updatedTask.setId(taskId);
        taskManager.updateTask(updatedTask);
        Task retrievedTask = taskManager.getTaskById(taskId);

        assertEquals("Новая задача", retrievedTask.getNameOfTask());
        assertEquals(TaskStatus.DONE, retrievedTask.getStatus());
        assertEquals("Новое описание", retrievedTask.getDescriptionOfTask());
        assertEquals(120, retrievedTask.getDuration().toMinutes());
        assertEquals(LocalDateTime.of(2025, 11, 2, 10, 0),
                retrievedTask.getStartTime());
    }

    @Test
    void testUpdateSubtask() {
        Subtask updatedSubtask = new Subtask("Новая задача", "Новое описание", TaskStatus.NEW,
                120, LocalDateTime.of(2025, 11, 2, 10, 0), epicId);
        updatedSubtask.setId(subtaskId);
        taskManager.updateSubtask(updatedSubtask);
        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);

        assertEquals("Новая задача", retrievedSubtask.getNameOfTask());
        assertEquals(TaskStatus.NEW, retrievedSubtask.getStatus());
        assertEquals("Новое описание", retrievedSubtask.getDescriptionOfTask());
        assertEquals(120, retrievedSubtask.getDuration().toMinutes());
        assertEquals(LocalDateTime.of(2025, 11, 2, 10, 0),
                retrievedSubtask.getStartTime());
    }

    @Test
    void testUpdateEpic() {
        Epic updatedEpic = new Epic("Новая задача", "Новое описание", TaskStatus.DONE,
                120, LocalDateTime.of(2025, 11, 2, 10, 0));
        updatedEpic.setId(epicId);
        taskManager.updateEpic(updatedEpic);
        Epic retrievedEpic = taskManager.getEpicById(epicId);

        assertEquals("Новая задача", retrievedEpic.getNameOfTask());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        assertEquals("Новое описание", retrievedEpic.getDescriptionOfTask());
        assertEquals(120, retrievedEpic.getDuration().toMinutes());
        assertEquals(LocalDateTime.of(2025, 11, 2, 10, 0),
                retrievedEpic.getStartTime());
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task2 = taskManager.addTask(new Task("Без времени", "", TaskStatus.NEW,
                30, null));

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task, prioritized.get(0));
        assertEquals(subtask, prioritized.get(1));
    }
}