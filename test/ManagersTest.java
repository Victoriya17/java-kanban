import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    Managers managers = new Managers();

    @Test
    void getWorkTaskManager() {
        InMemoryTaskManager memoryTaskManager = (InMemoryTaskManager) managers.getDefault();
        assertNotNull(memoryTaskManager, "Экземпляр com.yandex.app.service.InMemoryTaskManager должен быть " +
                "не null");
    }

    @Test
    void getWorkHistoryManager() {
        InMemoryHistoryManager historyManager = (InMemoryHistoryManager) managers.getDefaultHistory();
        assertNotNull(historyManager, "Экземпляр com.yandex.app.service.InMemoryHistoryManager должен быть " +
                "не null");
    }
}