package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class FileTransferAcknowledgePacket extends BasePacket {
    public final PacketType packetType = PacketType.FileTransferAcknowledgePacket;

    public FileTransferAcknowledgePacket() {
        super();
        this.length = ByteBuffer.allocate(8).putLong(12).array();
        this.id = packetType.toByte();
    }
}
