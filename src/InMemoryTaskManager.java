import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    public HistoryManager historyManager = new InMemoryHistoryManager();

    public static int getNextId() {
        return nextId++;
    }

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    @Override
    public Task addTask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus) {
        int taskId = getNextId();
        Task newTask = new Task(nameOfTask, descriptionOfTask, taskId, taskStatus);
        tasks.put(taskId, newTask);
        return newTask;
    }

    @Override
    public Subtask addSubtask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus, Epic epic) {
        int subtaskId = getNextId();
        Subtask newSubtask = new Subtask(nameOfTask, descriptionOfTask, subtaskId, taskStatus, epic);
        subtasks.put(subtaskId, newSubtask);
        epic.addSubtask(newSubtask);
        epic.updateEpicStatus();
        return newSubtask;
    }

    @Override
    public Epic addEpic(String nameOfTask, String descriptionOfTask, TaskStatus status) {
        int epicId = getNextId();
        Epic newEpic = new Epic(nameOfTask, descriptionOfTask, epicId, status);
        epics.put(epicId, newEpic);
        return newEpic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks(int epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpic().getId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtask();
            epic.updateEpicStatus();
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            deleteAllSubtasks();
        }
        epics.clear();
    }

    @Override
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        subtasks.remove(id);
    }

    @Override
    public void deleteEpicById(int epicId, Epic epic) {
        List<Subtask> subtasksToDelete = getAllSubtasks(epicId);
        for (Subtask subtask : subtasksToDelete) {
            subtasks.remove(subtask.getId());
            epic.deleteSubtask(subtask.getId());
        }
        epics.remove(epicId);
        epic.updateEpicStatus();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateSubtaskStatus(subtask.getId(), subtask.getStatus());
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        epic.updateEpicStatus();
    }

    @Override
    public void updateSubtaskStatus(int subtaskId, TaskStatus newStatus) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            subtask.setStatus(newStatus);
            Epic epic = subtask.getEpic();
            epic.updateEpicStatus();
        }
    }
}