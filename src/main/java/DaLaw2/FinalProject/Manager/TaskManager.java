package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Manager.DataClass.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskManager {
    private static volatile TaskManager instance = new TaskManager();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private static final Logger logger = LogManager.getLogger(TaskManager.class);

    private final HashMap<UUID, Task> tasks = new HashMap<>();

    private TaskManager() {
        Optional<HashMap<UUID, Task>> parseFromFile = tryParseFromFile();
        HashMap<UUID, Task> existingTasks = parseFromFile.orElseGet(HashMap::new);
        for (Task task : existingTasks.values())
            addTask(task);
    }

    public static TaskManager getInstance() {
        return instance;
    }

    public void addTask(Task task) {
        rwLock.writeLock().lock();
        tasks.put(task.uuid, task);
        rwLock.writeLock().unlock();
    }

    public void processTask(Task task) {

    }

    private Optional<HashMap<UUID, Task>> tryParseFromFile() {
        try {
            String fileName = ".tasks";
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            @SuppressWarnings("unchecked")
            HashMap<UUID, Task> result = (HashMap<UUID, Task>) in.readObject();
            return Optional.of(result);
        } catch (IOException | ClassNotFoundException | ClassCastException _) {
            return Optional.empty();
        }
    }

    private void dumpToFile(HashMap<UUID, Task> tasks) throws IOException {
        String fileName = ".tasks";
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(tasks);
    }
}
