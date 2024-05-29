package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class BasePacket {
    public byte[] length;
    public byte[] id;
    public byte[] data;
    public PacketType packetType = PacketType.BasePacket;

    public BasePacket() {}

    public BasePacket(byte[] data) {
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.id = packetType.toByte();
        this.data = data;
    }

    public BasePacket(byte[] length, byte[] id, byte[] data) {
        this.length = length;
        this.id = id;
        this.data = data;
    }
}
