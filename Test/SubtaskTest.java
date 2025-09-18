import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void cannotAddSubtaskToItEpic() {
        Epic epic = new Epic("name", "description", 1, TaskStatus.NEW);
        Subtask subtask = new Subtask("name", "description", 2, TaskStatus.NEW, epic);
        subtask.setEpic(epic);
        assertEquals(epic, subtask.getEpic(), "Подзадача не может быть добавлена в свой же Epic");
    }

    @Test
    void SubtaskEqualitySubtask() {
        Epic epic = new Epic("name", "description", 1, TaskStatus.NEW);
        Subtask subtask1 = new Subtask("name", "description", 2, TaskStatus.NEW, epic);
        Subtask subtask2 = new Subtask("name", "description", 2, TaskStatus.NEW, epic);

        assertEquals(subtask1, subtask2, "Subtask с одинаковыми ID должны быть равны.");
    }

}