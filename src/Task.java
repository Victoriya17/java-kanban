import java.util.Objects;

public class Task {
    public String nameOfTask;
    public String descriptionOfTask;
    public int id;

    public enum TaskStatus {
        NEW,
        IN_PROGRESS,
        DONE
    }

    private TaskStatus status;

    public String getNameOfTask() {
        return nameOfTask;
    }

    public String getDescriptionOfTask() {
        return descriptionOfTask;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task otherTask = (Task) obj;
        return Objects.equals(id, otherTask.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    public Task(String nameOfTask, String descriptionOfTask, int id, TaskStatus status) {
        this.nameOfTask = nameOfTask;
        this.descriptionOfTask = descriptionOfTask;
        this.id = id;
        this.status = status;
    }
}