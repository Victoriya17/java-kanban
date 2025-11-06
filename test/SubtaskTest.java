import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Задача", TaskStatus.NEW, "Описание задачи", 90,
                LocalDateTime.of(2025, 11, 1, 9, 0));
        epic.setId(1);
        subtask = new Subtask("Задача", TaskStatus.NEW, "Описание задачи", 90,
                LocalDateTime.of(2025, 11, 1, 9, 0), epic.getId());
        subtask.setId(2);

    }

    @Test
    void cannotAddSubtaskToItEpic() {
        subtask.setEpicId(epic.getId());
        assertEquals(epic.getId(), subtask.getEpicId(), "Подзадача не может быть добавлена в свой же Epic");
    }

    @Test
    void SubtaskEqualitySubtask() {
        Subtask subtask2 = new Subtask("Задача", TaskStatus.NEW, "Описание задачи",
                90, LocalDateTime.of(2025, 11, 1, 9, 0), epic.getId());
        subtask2.setId(subtask.getId());
        assertEquals(subtask, subtask2, "Subtask с одинаковыми ID должны быть равны.");
    }


    @Test
    void checkSetDuration() {
        subtask.setDuration(Duration.ofMinutes(15));
        assertEquals(Duration.ofMinutes(15), subtask.getDuration(), "Ошибка чтения продолжительности задачи");
    }

    @Test
    void checkSetStartTime() {
        subtask.setStartTime(LocalDateTime.of(2025, 11, 2, 9, 0));
        assertEquals(LocalDateTime.of(2025, 11, 2, 9, 0), subtask.getStartTime(),
                "Ошибка чтения времени старта задачи");
    }

    @Test
    void checkEndTime() {
        assertEquals(subtask.getStartTime().plusMinutes(subtask.getDurationToMinutes()), subtask.getEndTime(),
                "Ошибка чтения времени окончания задачи");
    }
}