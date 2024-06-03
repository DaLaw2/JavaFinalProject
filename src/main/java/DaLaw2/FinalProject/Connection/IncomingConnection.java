package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.*;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import DaLaw2.FinalProject.Manager.DataClass.FileBody;
import DaLaw2.FinalProject.Manager.DataClass.FileHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        BasePacket packet = socket.receivePacket();
        FileHeaderPacket fileHeaderPacket = FileHeaderPacket.fromBasePacket(packet);
        FileHeader fileHeader = fileHeaderPacket.unwarp();
        this.socket = socket;
        this.uuid = fileHeader.uuid;
        this.fileName = fileHeader.fileName;
        this.fileSize = fileHeader.fileSize;
        this.totalBlocks = fileHeader.packetCount;
        this.receivedBlocks = getExistBlock();
    }

    @Override
    public void run() {
        Config config = ConfigManager.getConfig();
        try {
            while (receivedBlocks.size() < totalBlocks) {
                sendRequest();
                while (true) {
                    BasePacket packet = socket.receivePacket();
                    PacketType packetType = PacketType.fromByte(packet.id);
                    if (packetType == PacketType.FileBodyPacket) {
                        FileBodyPacket fileBodyPacket = FileBodyPacket.fromBasePacket(packet);
                        FileBody fileBody = fileBodyPacket.unwarp();
                        receivedBlocks.put(fileBody.id, fileBody.data);
                        Path savePath = config.savePath.resolve(uuid.toString()).resolve(String.valueOf(fileBody.id));
                        Files.write(savePath, fileBody.data);
                    } else if (packetType == PacketType.EndTransferPacket) {
                        break;
                    } else {
                        logger.error("Unexpected packet type: {}", packetType);
                        return;
                    }
                }
            }
            sendEndTransfer();
        } catch (SocketTimeoutException e) {
            logger.error("Timeout while receiving file", e);
        } catch (Exception e) {
            logger.error("Error while receiving file", e);
        }
    }

    private HashMap<Long, byte[]> getExistBlock() throws IOException {
        Config config = ConfigManager.getConfig();
        Path path = config.savePath.resolve(uuid.toString());

        if (!Files.exists(path))
            Files.createDirectories(path);
        else if (!Files.isDirectory(path))
            throw new IOException("Save path is not a directory.");

        File[] files = new File(path.toString()).listFiles();
        HashMap<Long, byte[]> receivedBlocks = new HashMap<>();
        assert files != null;
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

    private ArrayList<Long> findMissingBlocks() {
        ArrayList<Long> missingBlocks = new ArrayList<>();
        for (long i = 0; i < totalBlocks; i++)
            if (!receivedBlocks.containsKey(i))
                missingBlocks.add(i);
        return missingBlocks;
    }

    private void sendRequest() throws IOException {
        ArrayList<Long> missingBlocks = findMissingBlocks();
        FileRequireSendPacket fileRequireSendPacket = new FileRequireSendPacket(missingBlocks);
        socket.sendPacket(fileRequireSendPacket);
    }

    private void sendEndTransfer() throws IOException {
        EndTransferPacket endTransferPacket = new EndTransferPacket();
        socket.sendPacket(endTransferPacket);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getTotalBlocks() {
        return totalBlocks;
    }

    public long getReceivedCount() {
        return receivedBlocks.size();
    }
}
