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
    public void testTaskImmutability() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        task1 = new Task("name", "description", 1, TaskStatus.NEW);
        Task addedTask = taskManager.addTask(task1.getNameOfTask(), task1.getDescriptionOfTask(), task1.getStatus());

        assertEquals(task1.getNameOfTask(), addedTask.getNameOfTask());
        assertEquals(task1.getDescriptionOfTask(), addedTask.getDescriptionOfTask());
        assertEquals(task1.getStatus(), addedTask.getStatus());
    }
}