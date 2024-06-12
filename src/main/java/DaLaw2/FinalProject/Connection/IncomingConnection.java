package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.*;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import DaLaw2.FinalProject.Manager.DataClass.FileBody;
import DaLaw2.FinalProject.Manager.DataClass.FileHeader;
import DaLaw2.FinalProject.Manager.TaskManager;
import org.apache.commons.io.FileUtils;
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
    private final Path tempDirectory;

    public IncomingConnection(SocketStream socket) throws IOException, ClassNotFoundException {
        Config config = ConfigManager.getConfig();
        BasePacket packet = socket.receivePacket();
        FileHeaderPacket fileHeaderPacket = FileHeaderPacket.fromBasePacket(packet);
        FileHeader fileHeader = fileHeaderPacket.unwarp();
        this.socket = socket;
        this.uuid = fileHeader.uuid;
        this.fileName = fileHeader.fileName;
        this.fileSize = fileHeader.fileSize;
        this.totalBlocks = fileHeader.packetCount;
        this.tempDirectory = Path.of(config.savePath, uuid.toString());
        createTempDirectory();
    }

    @Override
    public void run() {
        try {
            while (getReceivedCount() < totalBlocks) {
                sendRequest();
                while (true) {
                    BasePacket packet = socket.receivePacket();
                    PacketType packetType = PacketType.fromByte(packet.id);
                    if (packetType == PacketType.FileBodyPacket) {
                        FileBodyPacket fileBodyPacket = FileBodyPacket.fromBasePacket(packet);
                        FileBody fileBody = fileBodyPacket.unwarp();
                        Path savePath = tempDirectory.resolve(String.valueOf(fileBody.id));
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
            deleteTempDirectory();
            markTaskComplete();
        } catch (SocketTimeoutException e) {
            logger.error("Timeout while receiving file: {}", e.getMessage());
            markTaskFailed();
        } catch (Exception e) {
            logger.error("Error while receiving file: {}", e.getMessage());
            markTaskFailed();
        }
    }

    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Failed to close socket: {}", e.getMessage());
        }
    }

    private void createTempDirectory() throws IOException {
        if (!Files.exists(tempDirectory)) {
            Files.createDirectories(tempDirectory);
        } else if (!Files.isDirectory(tempDirectory)) {
            throw new IOException("Temp path is not a directory");
        } else {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        }
    }

    private long getReceivedCount() {
        return Objects.requireNonNull(tempDirectory.toFile().listFiles()).length;
    }

    private ArrayList<Long> findMissingBlocks() {
        ArrayList<Long> missingBlocks = new ArrayList<>();
        for (long i = 0; i < totalBlocks; i++) {
            Path blockPath = tempDirectory.resolve(String.valueOf(i));
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
        Path blockPath = tempDirectory.resolve(String.valueOf(blockId));
        return Files.readAllBytes(blockPath);
    }

    private void createFile() throws IOException {
        Config config = ConfigManager.getConfig();
        Path tempDirectory = Path.of(config.savePath, uuid.toString());
        try (FileOutputStream stream = new FileOutputStream(tempDirectory.toFile())) {
            for (long i = 0; i < totalBlocks; i++)
                stream.write(readFileBlock(i));
        }
    }

    private void deleteTempDirectory() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    private void markTaskComplete() {
        TaskManager.getInstance().getTask(uuid).complete();
    }

    private void markTaskFailed() {
        TaskManager.getInstance().getTask(uuid).fail();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public Path getFilePath() {
        return tempDirectory;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getTotalBlocks() {
        return totalBlocks;
    }
}
