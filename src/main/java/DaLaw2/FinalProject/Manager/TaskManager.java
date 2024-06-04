package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Manager.DataClass.Config;
import DaLaw2.FinalProject.Manager.DataClass.Task;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskManager {
    private static volatile TaskManager instance = new TaskManager();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

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

    public void createSendTask(String host, int port, Path sourcePath) {
        UUID uuid = UUID.randomUUID();
        String fileName = sourcePath.getFileName().toString();
        Task sendTask = Task.createSendTask(uuid, host, port, fileName);
        addTask(sendTask);
        ConnectionManager.getInstance().startConnection(uuid, host, port, sourcePath);
    }

    public void createReceiveTask(UUID uuid, String host, int port, String fileName) {
        Task receiveTask = Task.createReceiveTask(uuid, host, port, fileName);
        addTask(receiveTask);
    }
}
