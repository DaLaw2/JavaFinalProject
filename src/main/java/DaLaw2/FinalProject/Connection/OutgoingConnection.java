package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.*;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.DataClass.FileBody;
import DaLaw2.FinalProject.Manager.DataClass.FileHeader;
import DaLaw2.FinalProject.Manager.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class OutgoingConnection extends Thread {
    private static final Logger logger = LogManager.getLogger(OutgoingConnection.class);

    private final UUID uuid;
    private final SocketStream socket;
    private final Path sourcePath;
    private final String fileName;
    private final long fileSize;

    public OutgoingConnection(UUID uuid, SocketStream socket, Path sourcePath) throws IOException {
        this.uuid = uuid;
        this.socket = socket;
        this.sourcePath = sourcePath;
        this.fileName = sourcePath.getFileName().toString();
        this.fileSize = sourcePath.toFile().length();
        sendFileHeader();
    }

    @Override
    public void run() {
        try {
            while (true) {
                BasePacket packet = socket.receivePacket();
                PacketType packetType = PacketType.fromByte(packet.id);
                if (packetType == PacketType.FileRequireSendPacket) {
                    FileRequireSendPacket fileRequireSendPacket = FileRequireSendPacket.fromBasePacket(packet);
                    ArrayList<Long> requireSend = fileRequireSendPacket.unwarp();
                    for (Long blockId : requireSend) {
                        byte[] data = readFileBlock(blockId);
                        FileBody fileBody = new FileBody(blockId, data);
                        FileBodyPacket fileBodyPacket = new FileBodyPacket(fileBody);
                        socket.sendPacket(fileBodyPacket);
                    }
                    sendEndTransfer();
                } else if (packetType == PacketType.EndTransferPacket) {
                    break;
                } else {
                    logger.error("Unexpected packet type: {}", packetType);
                    return;
                }
            }
            closeConnection();
            markTaskComplete();
        } catch (SocketTimeoutException e) {
            logger.error("Timeout while send file: {}", e.getMessage());
            markTaskFailed();
        } catch (Exception e) {
            logger.error("Error while send file: {}", e.getMessage());
            markTaskFailed();
        }
    }

    public void shutdown() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Failed to close socket: {}", e.getMessage());
        }
    }

    private void sendFileHeader() throws IOException {
        FileHeader fileHeader = new FileHeader(uuid, fileName, fileSize);
        FileHeaderPacket fileHeaderPacket = new FileHeaderPacket(fileHeader);
        socket.sendPacket(fileHeaderPacket);
    }

    private byte[] readFileBlock(long blockId) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(sourcePath.toFile(), "r")) {
            int blockSize = 1024 * 1024;
            file.seek(blockId * blockSize);

            long remainingBytes = fileSize - blockId * blockSize;
            int bytesToRead = (int) Math.min(blockSize, remainingBytes);

            byte[] data = new byte[bytesToRead];
            file.readFully(data, 0, bytesToRead);

            return data;
        }
    }

    private void sendEndTransfer() throws IOException {
        EndTransferPacket endTransferPacket = new EndTransferPacket();
        socket.sendPacket(endTransferPacket);
    }

    private void closeConnection() throws IOException {
        socket.close();
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
}