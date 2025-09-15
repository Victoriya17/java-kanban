import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void EpicEqualityEpic() {
        Epic epic1 = new Epic("name", "description", 1, TaskStatus.NEW);
        Epic epic2 = new Epic("name", "description", 1, TaskStatus.NEW);

        assertEquals(epic1, epic2, "Эпики с одинаковыми ID должны быть равны.");
    }

    @Test
    void cannotAddEpicToItselfAsSubtask() {
        Epic epic = new Epic("name", "description", 1, TaskStatus.NEW);
        int size = epic.getSubtasks().size();

        epic.addSubtask(new Subtask("name", "description", 2, TaskStatus.NEW, epic));

        assertEquals(size, epic.getSubtasks().size(), "Размер списка подзадач не изменился при " +
                "попытке добавления Эпика в себя, в виде подзадачи");
    }

}