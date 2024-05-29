package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class FileBodyPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileBodyPacket;

    public FileBodyPacket(byte[] data) {
        super();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.id = packetType.toByte();
    }
}
