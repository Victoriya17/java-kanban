import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private List<Object> history = new ArrayList<>();

    @Override
    public <T extends Task> void add(T task) {
        history.add(task.copy());
        if (history.size() > 10) {
            history.remove(0);
        }
    }

    @Override
    public List<Object> getHistory() {
        return history;
    }
}
