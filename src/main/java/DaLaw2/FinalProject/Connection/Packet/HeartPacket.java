package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class HeartPacket extends BasePacket {
    public final PacketType packetType = PacketType.HeartPacket;

    public HeartPacket() {
        super();
        this.length = ByteBuffer.allocate(8).putLong(12).array();
        this.id = packetType.toByte();
    }
}
