package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class FileHeaderPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileHeaderPacket;

    public FileHeaderPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }
}
