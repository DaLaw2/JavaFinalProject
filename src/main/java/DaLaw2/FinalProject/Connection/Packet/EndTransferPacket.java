package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class EndTransferPacket extends BasePacket {
    public final PacketType packetType = PacketType.EndTransferPacket;

    public EndTransferPacket() {
        super();
        this.id = packetType.toByte();
        this.length = ByteBuffer.allocate(8).putLong(12).array();
        this.data = new byte[0];
    }
}
