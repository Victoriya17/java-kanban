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

    public Task addTask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus) {
        int taskId = getNextId();
        Task newTask = new Task(nameOfTask, descriptionOfTask, taskId, taskStatus);
        tasks.put(taskId, newTask);
        return newTask;
    }

    public Subtask addSubtask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus, Epic epic) {
        int subtaskId = getNextId();
        Subtask newSubtask = new Subtask(nameOfTask, descriptionOfTask, subtaskId, taskStatus, epic);
        subtasks.put(subtaskId, newSubtask);
        epic.addSubtask(newSubtask);
        epic.updateEpicStatus();
        return newSubtask;
    }

    public Epic addEpic(String nameOfTask, String descriptionOfTask, TaskStatus status) {
        int epicId = getNextId();
        Epic newEpic = new Epic(nameOfTask, descriptionOfTask, epicId, status);
        epics.put(epicId, newEpic);
        return newEpic;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getAllSubtasks(int epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpic().getId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtask();
            epic.updateEpicStatus();
        }
    }

    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            deleteAllSubtasks();
        }
        epics.clear();
    }

    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    public void deleteSubtaskById(Integer id) {
        subtasks.remove(id);
    }

    public void deleteEpicById(int epicId, Epic epic) {
        List<Subtask> subtasksToDelete = getAllSubtasks(epicId);
        for (Subtask subtask : subtasksToDelete) {
            subtasks.remove(subtask.getId());
            epic.deleteSubtask(subtask.getId());
        }
        epics.remove(epicId);
        epic.updateEpicStatus();
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(Integer id) {
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

    public void updateSubtaskStatus(int subtaskId, TaskStatus newStatus) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            subtask.setStatus(newStatus);
            Epic epic = subtask.getEpic();
            epic.updateEpicStatus();
        }
    }
}