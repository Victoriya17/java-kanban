import java.util.List;

public interface TaskManager {
    Task addTask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus);

    Subtask addSubtask(String nameOfTask, String descriptionOfTask, TaskStatus taskStatus, Epic epic);

    Epic addEpic(String nameOfTask, String descriptionOfTask, TaskStatus status);

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks(int epicId);

    List<Epic> getAllEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteTaskById(Integer id);

    void deleteSubtaskById(Integer id);

    void deleteEpicById(int epicId, Epic epic);

    Task getTaskById(Integer id);

    Subtask getSubtaskById(Integer id);

    Epic getEpicById(Integer id);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void updateSubtaskStatus(int subtaskId, TaskStatus newStatus);
}
