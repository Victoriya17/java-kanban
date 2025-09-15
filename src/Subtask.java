public class Subtask extends Task {
    private Epic epic;

    public Subtask(String nameOfTask, String descriptionOfTask, int id, TaskStatus status, Epic epic) {
        super(nameOfTask, descriptionOfTask, id, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public void setStatus(TaskStatus newStatus) {
        super.setStatus(newStatus);
        if (this.epic != null) {
            this.epic.updateEpicStatus();
        }
    }

    @Override
    public Subtask copy() {
        return new Subtask(getNameOfTask(), getDescriptionOfTask(), getId(), getStatus(), epic);
    }
}
