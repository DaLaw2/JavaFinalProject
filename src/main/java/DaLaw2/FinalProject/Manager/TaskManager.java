package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Manager.DataClass.Task;

import java.io.*;
import java.util.UUID;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskManager {
    private static volatile TaskManager instance;
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final HashMap<UUID, Task> tasks;

    private TaskManager() {
        Optional<HashMap<UUID, Task>> parseFromFile = tryParseFromFile();
        tasks = parseFromFile.orElseGet(HashMap::new);
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            rwLock.writeLock().lock();
            try {
                if (instance == null) {
                    instance = new TaskManager();
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        return instance;
    }

    private Optional<HashMap<UUID, Task>> tryParseFromFile() {
        try {
            String fileName = ".tasks";
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            @SuppressWarnings("unchecked")
            HashMap<UUID, Task> result = (HashMap<UUID, Task>) in.readObject();
            return Optional.of(result);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
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
