package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public class Task implements Serializable {
    public enum TaskType {
        Send,
        Receive,
    }

    public final UUID uuid;
    public final TaskType type;
    public final Path sourcePath;
    public final Path savePath;
    public Set<Long> receivedBlocks;

    private Task(UUID uuid, TaskType type, Path path) {
        this.uuid = uuid;
        this.receivedBlocks = Set.of();
        this.type = type;
        if (type == TaskType.Send) {
            this.sourcePath = path;
            this.savePath = null;
        } else {
            this.sourcePath = null;
            this.savePath = path;
        }
    }

    public static Task createSendTask(UUID uuid, Path sourcePath) {
        return new Task(uuid ,TaskType.Send, sourcePath);
    }

    public static Task createReceiveTask(UUID uuid, Path savePath) {
        return new Task(uuid, TaskType.Receive, savePath);
    }
}
