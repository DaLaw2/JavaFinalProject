package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.UUID;

public class Task implements Serializable {
    public enum TaskType {
        Send,
        Receive,
    }

    public enum TaskStatus {
        Completed,
        InProgress,
        Failed,
    }

    public final UUID uuid;
    public final TaskType type;
    public TaskStatus status;
    public final String host;
    public final int port;
    public final String filePath;
    public final String fileName;

    private Task(UUID uuid, TaskType type, String host, int port, Path filePath) {
        this.uuid = uuid;
        this.type = type;
        this.status = TaskStatus.InProgress;
        this.host = host;
        this.port = port;
        this.filePath = filePath.toString();
        this.fileName = filePath.getFileName().toString();
    }

    public static Task createSendTask(UUID uuid, String host, int port, Path filePath) {
        return new Task(uuid, TaskType.Send, host, port, filePath);
    }

    public static Task createReceiveTask(UUID uuid, String host, int port, Path filePath) {
        return new Task(uuid, TaskType.Receive, host, port, filePath);
    }

    public void complete() {
        status = TaskStatus.Completed;
    }

    public void fail() {
        status = TaskStatus.Failed;
    }
}
