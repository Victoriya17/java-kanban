import com.yandex.app.exceptions.TimeOverlapException;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private Task task1;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("Задача", "Описание задачи", TaskStatus.NEW, 90,
                LocalDateTime.of(2025, 11, 1, 9, 0));
        task1.setId(1);
    }

    @Test
    void TaskEqualityTask() {
        Task task2 = new Task("Задача", "Описание задачи", TaskStatus.NEW, 90,
                LocalDateTime.now());
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковыми ID должны быть равны.");
    }

    @Test
    void testTaskImmutability() throws TimeOverlapException {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task addedTask = new Task(task1.getNameOfTask(), task1.getDescriptionOfTask(), task1.getStatus(),
                task1.getDurationToMinutes(), task1.getStartTime());
        taskManager.addTask(addedTask);

        assertEquals(task1.getNameOfTask(), addedTask.getNameOfTask());
        assertEquals(task1.getDescriptionOfTask(), addedTask.getDescriptionOfTask());
        assertEquals(task1.getStatus(), addedTask.getStatus());
        assertEquals(task1.getDurationToMinutes(), addedTask.getDurationToMinutes());
        assertEquals(task1.getStartTime(), addedTask.getStartTime());
    }

    @Test
    void checkSetDuration() {
        task1.setDuration(Duration.ofMinutes(15));
        assertEquals(Duration.ofMinutes(15), task1.getDuration(), "Ошибка чтения продолжительности задачи");
    }

    @Test
    void checkSetStartTime() {
        task1.setStartTime(LocalDateTime.of(2025, 11, 2, 9, 0));
        assertEquals(LocalDateTime.of(2025, 11, 2, 9, 0), task1.getStartTime(),
                "Ошибка чтения времени старта задачи");
    }

    @Test
    void checkEndTime() {
        assertEquals(task1.getStartTime().plusMinutes(task1.getDurationToMinutes()), task1.getEndTime(),
                "Ошибка чтения времени окончания задачи");
    }
}