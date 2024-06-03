package DaLaw2.FinalProject.Manager.DataClass;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileHeader implements Serializable {
    public UUID uuid;
    public String fileName;
    public long fileSize;
    public long packetCount;

    public FileHeader(UUID uuid, String fileName, long fileSize) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.packetCount = (long) Math.ceil((double) fileSize / 1048576);
    }
}
