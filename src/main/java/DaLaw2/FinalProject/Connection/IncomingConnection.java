package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.*;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import DaLaw2.FinalProject.Manager.DataClass.FileBody;
import DaLaw2.FinalProject.Manager.DataClass.FileHeader;
import DaLaw2.FinalProject.Manager.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class IncomingConnection extends Thread {
    private static final Logger logger = LogManager.getLogger(IncomingConnection.class);

    private final UUID uuid;
    private final SocketStream socket;
    private final String fileName;
    private final long fileSize;
    private final long totalBlocks;
    private final Path savePath;

    public IncomingConnection(SocketStream socket) throws IOException, ClassNotFoundException {
        BasePacket packet = socket.receivePacket();
        FileHeaderPacket fileHeaderPacket = FileHeaderPacket.fromBasePacket(packet);
        FileHeader fileHeader = fileHeaderPacket.unwarp();
        this.socket = socket;
        this.uuid = fileHeader.uuid;
        this.fileName = fileHeader.fileName;
        this.fileSize = fileHeader.fileSize;
        this.totalBlocks = fileHeader.packetCount;
        this.savePath = ConfigManager.getConfig().savePath.resolve(uuid.toString());
        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        } else if (!Files.isDirectory(savePath)) {
            throw new IOException("Save path is not a directory.");
        }
    }

    @Override
    public void run() {
        Config config = ConfigManager.getConfig();
        try {
            while (getReceivedCount() < totalBlocks) {
                sendRequest();
                while (true) {
                    BasePacket packet = socket.receivePacket();
                    PacketType packetType = PacketType.fromByte(packet.id);
                    if (packetType == PacketType.FileBodyPacket) {
                        FileBodyPacket fileBodyPacket = FileBodyPacket.fromBasePacket(packet);
                        FileBody fileBody = fileBodyPacket.unwarp();
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
            endTransfer();
            createFile();
            clearTempFiles();
        } catch (SocketTimeoutException e) {
            logger.error("Timeout while receiving file", e);
        } catch (Exception e) {
            logger.error("Error while receiving file", e);
        }
    }

    private ArrayList<Long> findMissingBlocks() {
        ArrayList<Long> missingBlocks = new ArrayList<>();
        for (long i = 0; i < totalBlocks; i++) {
            Path blockPath = savePath.resolve(String.valueOf(i));
            if (!Files.exists(blockPath))
                missingBlocks.add(i);
        }
        return missingBlocks;
    }

    private void sendRequest() throws IOException {
        ArrayList<Long> missingBlocks = findMissingBlocks();
        FileRequireSendPacket fileRequireSendPacket = new FileRequireSendPacket(missingBlocks);
        socket.sendPacket(fileRequireSendPacket);
    }

    private void endTransfer() throws IOException {
        EndTransferPacket endTransferPacket = new EndTransferPacket();
        socket.sendPacket(endTransferPacket);
        socket.close();
    }

    private byte[] readFileBlock(long blockId) throws IOException {
        Path blockPath = savePath.resolve(String.valueOf(blockId));
        return Files.readAllBytes(blockPath);
    }

    private void createFile() throws IOException {
        Config config = ConfigManager.getConfig();
        Path savePath = config.savePath.resolve(fileName);
        try (FileOutputStream stream = new FileOutputStream(savePath.toFile())) {
            for (long i = 0; i < totalBlocks; i++)
                stream.write(readFileBlock(i));
        }
    }

    private void clearTempFiles() throws IOException {
        for (long i = 0; i < totalBlocks; i++) {
            Path blockPath = savePath.resolve(String.valueOf(i));
            Files.delete(blockPath);
        }
        Files.delete(savePath);
    }

    private void markedFail() {
        TaskManager.getInstance().getTasks()
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

    private long getReceivedCount() {
        return Objects.requireNonNull(savePath.toFile().listFiles()).length;
    }
}
