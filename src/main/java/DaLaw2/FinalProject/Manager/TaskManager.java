package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Manager.DataClass.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TaskManager {
    private static final Logger logger = LogManager.getLogger(TaskManager.class);

    private static final TaskManager instance = new TaskManager();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final HashMap<UUID, Task> tasks = new HashMap<>();

    private TaskManager() {
        Optional<HashMap<UUID, Task>> parseFromFile = tryParseFromFile();
        HashMap<UUID, Task> existingTasks = parseFromFile.orElseGet(HashMap::new);
        for (Task task : existingTasks.values())
            if (task.status == Task.TaskStatus.InProgress || task.status == Task.TaskStatus.Failed)
                if (task.type == Task.TaskType.Send)
                    ConnectionManager.getInstance().startConnection(task.uuid, task.host, task.port, Path.of(task.filePath));
    }

    public static TaskManager getInstance() {
        return instance;
    }

    public Task getTask(UUID uuid) {
        rwLock.readLock().lock();
        Task result = tasks.get(uuid);
        rwLock.readLock().unlock();
        return result;
    }

    public HashMap<UUID, Task> getTasks() {
        rwLock.readLock().lock();
        HashMap<UUID, Task> result = new HashMap<>(tasks);
        rwLock.readLock().unlock();
        return result;
    }

    public void addTask(Task task) {
        rwLock.writeLock().lock();
        tasks.put(task.uuid, task);
        rwLock.writeLock().unlock();
    }

    private static Optional<HashMap<UUID, Task>> tryParseFromFile() {
        try {
            String fileName = ".tasks";
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            @SuppressWarnings("unchecked")
            HashMap<UUID, Task> result = (HashMap<UUID, Task>) in.readObject();
            return Optional.of(result);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            logger.info("No exist tasks file found.");
            return Optional.empty();
        }
    }

    public static void dumpToFile() throws IOException {
        HashMap<UUID, Task> tasks = TaskManager.getInstance().getTasks();
        String fileName = ".tasks";
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(tasks);
    }

    public void createSendTask(String host, int port, Path sourcePath) {
        UUID uuid = UUID.randomUUID();
        Task sendTask = Task.createSendTask(uuid, host, port, sourcePath);
        addTask(sendTask);
        ConnectionManager.getInstance().startConnection(uuid, host, port, sourcePath);
    }

    public void createReceiveTask(UUID uuid, String host, int port, Path sourcePath) {
        Task receiveTask = Task.createReceiveTask(uuid, host, port, sourcePath);
        addTask(receiveTask);
    }
}
