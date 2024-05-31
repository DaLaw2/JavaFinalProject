package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.BasePacket;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Utils.Config;
import DaLaw2.FinalProject.Utils.FileHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class IncomingConnection extends Thread {
    private static final Logger logger = LogManager.getLogger(IncomingConnection.class);

    private final UUID uuid;
    private final SocketStream socket;
    private final String fileName;
    private final long fileSize;
    private final long totalBlocks;
    private final HashMap<Long, byte[]> receivedBlocks;

    public IncomingConnection(SocketStream socket) throws IOException, ClassNotFoundException {
        BasePacket headerPacket = socket.receivePacket();
        ByteArrayInputStream stream = new ByteArrayInputStream(headerPacket.data);
        ObjectInput in = new ObjectInputStream(stream);
        FileHeader fileHeader = (FileHeader) in.readObject();
        this.socket = socket;
        this.uuid = fileHeader.uuid;
        this.fileName = fileHeader.fileName;
        this.fileSize = fileHeader.fileSize;
        this.totalBlocks = fileHeader.packetCount;
        this.receivedBlocks = getExistBlock();
    }

    @Override
    public void run() {

    }

    private HashMap<Long, byte[]> getExistBlock() throws IOException {
        Config config = ConfigManager.getConfig();
        Path path = config.savePath.resolve(uuid.toString());

        if (!Files.exists(path))
            Files.createDirectories(path);

        File[] files = new File(path.toString()).listFiles();
        HashMap<Long, byte[]> receivedBlocks = new HashMap<>();
        for (File file : files) {
            try {
                long blockNumber = Long.parseLong(file.getName());
                byte[] data = Files.readAllBytes(file.toPath());
                receivedBlocks.put(blockNumber, data);
            } catch (NumberFormatException _) {
            }
        }
        return receivedBlocks;
    }

    public UUID getUUID() {
        return uuid;
    }
}
