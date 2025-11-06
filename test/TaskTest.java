import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private Task task1;

    @Test
    void TaskEqualityTask() {
        task1 = new Task("name", "description", 1, TaskStatus.NEW);
        Task task2 = new Task("name", "description", 1, TaskStatus.NEW);

        assertEquals(task1, task2, "Задачи с одинаковыми ID должны быть равны.");
    }

    @Test
    void testTaskImmutability() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        task1 = new Task("name", "description", 1, TaskStatus.NEW);
        Task addedTask = new Task(task1.getNameOfTask(), task1.getDescriptionOfTask(), task1.getId(),
                task1.getStatus());
        taskManager.addTask(addedTask);

        assertEquals(task1.getNameOfTask(), addedTask.getNameOfTask());
        assertEquals(task1.getDescriptionOfTask(), addedTask.getDescriptionOfTask());
        assertEquals(task1.getStatus(), addedTask.getStatus());
    }
}