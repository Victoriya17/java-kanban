import com.yandex.app.model.Epic;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic1;

    @BeforeEach
    void beforeEach() {
        epic1 = new Epic("Задача", TaskStatus.NEW, "Описание задачи", 90,
                LocalDateTime.of(2025, 11, 1, 9, 0));
        epic1.setId(1);
    }

    @Test
    void EpicEqualityEpic() {
        Epic epic2 = new Epic("Задача", TaskStatus.NEW, "Описание задачи", 90,
                LocalDateTime.of(2025, 12, 1, 9, 0));
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковыми ID должны быть равны.");
    }

    @Test
    void cannotAddEpicToItselfAsSubtask() {
        int size = epic1.getSubtasks().size();
        epic1.addSubtaskId(epic1.getId());

        assertEquals(size, epic1.getSubtasks().size(), "Размер списка подзадач не изменился при " +
                "попытке добавления Эпика в себя, в виде подзадачи");
    }


    @Test
    void checkSetDuration() {
        epic1.setDuration(Duration.ofMinutes(15));
        assertEquals(Duration.ofMinutes(15), epic1.getDuration(), "Ошибка чтения продолжительности задачи");
    }

    @Test
    void checkSetStartTime() {
        epic1.setStartTime(LocalDateTime.of(2025, 11, 2, 9, 0));
        assertEquals(LocalDateTime.of(2025, 11, 2, 9, 0),
                epic1.getStartTime(), "Ошибка чтения времени старта задачи");
    }

    @Test
    void checkEndTime() {
        assertEquals(epic1.getStartTime().plusMinutes(epic1.getDurationToMinutes()), epic1.getEndTime(),
                "Ошибка чтения времени окончания задачи");
    }
}