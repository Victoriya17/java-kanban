import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class TaskManager {
    private static int nextId = 1;

    public static int getNextId() {
        return nextId++;
    }

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    public Task addTask(String nameOfTask, String descriptionOfTask, Task.TaskStatus status) {
        int taskId = getNextId();
        Task newTask = new Task(nameOfTask, descriptionOfTask, taskId, status);
        tasks.put(taskId, newTask);
        return newTask;
    }

    public Subtask addSubtask(String nameOfTask, String descriptionOfTask, Task.TaskStatus status, Epic epic) {
        int subtaskId = getNextId();
        Subtask newSubtask = new Subtask(nameOfTask, descriptionOfTask, subtaskId, status, epic);
        subtasks.put(subtaskId, newSubtask);
        epic.updateEpicStatus();
        return newSubtask;
    }

    public Epic addEpic(String nameOfTask, String descriptionOfTask, Task.TaskStatus status) {
        int epicId = getNextId();
        Epic newEpic = new Epic(nameOfTask, descriptionOfTask, epicId, status);
        epics.put(epicId, newEpic);
        return newEpic;
    }

    public List<Task> printAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> printAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    public void deleteSubtaskById(Integer id) {
        subtasks.remove(id);
    }

    public void deleteEpicById(Integer id) {
        epics.remove(id);
    }

    public Task printTaskById(Integer id) {
        return tasks.get(id);
    }

    public Subtask printSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    public Epic printEpicById(Integer id) {
        return epics.get(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateSubtaskStatus(subtask.getId(), subtask.getStatus());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        epic.updateEpicStatus();
    }

    public List<Subtask> printAllSubtasks(int epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpic().getId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    public void updateSubtaskStatus(int subtaskId, Task.TaskStatus newStatus) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            subtask.setStatus(newStatus);
            Epic epic = subtask.getEpic();
            epic.updateEpicStatus();
        }
    }
}
