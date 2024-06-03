package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Packet.*;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.DataClass.FileBody;
import DaLaw2.FinalProject.Manager.DataClass.FileHeader;
import DaLaw2.FinalProject.Utils.AppLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class OutgoingConnection extends Thread {
    private static final Logger logger = LogManager.getLogger(AppLogger.class);

    private final UUID uuid;
    private final SocketStream socket;
    private final Path sourcePath;
    private final String fileName;
    private final long fileSize;
    private final ArrayList<byte[]> fileData;

    public OutgoingConnection(UUID uuid, SocketStream socket, Path sourcePath) throws IOException {
        this.uuid = uuid;
        this.socket = socket;
        this.sourcePath = sourcePath;
        this.fileName = sourcePath.getFileName().toString();
        this.fileSize = sourcePath.toFile().length();
        this.fileData = readFile(sourcePath);
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
                        byte[] data = fileData.get(blockId.intValue());
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
        } catch (SocketTimeoutException e) {
            logger.error("Timeout while send file", e);
        } catch (Exception e) {
            logger.error("Error while send file", e);
        }
    }

    private ArrayList<byte[]> readFile(Path sourcePath) throws IOException {
        ArrayList<byte[]> blocks = new ArrayList<>();
        byte[] data = Files.readAllBytes(sourcePath);
        int blockSize = 1024 * 1024;
        int totalBlocks = (int) Math.ceil((double) data.length / blockSize);
        for (int i = 0; i < totalBlocks; i++) {
            int start = i * blockSize;
            int end = Math.min((i + 1) * blockSize, data.length);
            byte[] block = new byte[end - start];
            System.arraycopy(data, start, block, 0, end - start);
            blocks.add(block);
        }
        return blocks;
    }

    private void sendFileHeader() throws IOException {
        FileHeader fileHeader = new FileHeader(uuid, fileName, fileSize);
        FileHeaderPacket fileHeaderPacket = new FileHeaderPacket(fileHeader);
        socket.sendPacket(fileHeaderPacket);
    }

    private void sendEndTransfer() throws IOException {
        EndTransferPacket endTransferPacket = new EndTransferPacket();
        socket.sendPacket(endTransferPacket);
    }

    public UUID getUUID() {
        return uuid;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}