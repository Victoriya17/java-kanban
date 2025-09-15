import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String nameOfTask, String descriptionOfTask, int id, TaskStatus status) {
        super(nameOfTask, descriptionOfTask, id, status);
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getEpic() != this) {
            subtasks.add(subtask);
        }
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void deleteSubtask(int subtaskId) {
        subtasks.remove(subtaskId);
    }

    public void deleteAllSubtask() {
        subtasks.clear();
    }

    public void updateEpicStatus() {
        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (subtasks.isEmpty() || allNew) {
            this.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            this.setStatus(TaskStatus.DONE);
        } else {
            this.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public Epic copy() {
        return new Epic(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus());
    }
}
