package DaLaw2.FinalProject.Connection.Packet;

import java.nio.ByteBuffer;

public class BasePacket {
    public byte[] id;
    public byte[] length;
    public byte[] data;
    public PacketType packetType = PacketType.BasePacket;

    public BasePacket() {}

    public BasePacket(byte[] data) {
        this.id = packetType.toByte();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public BasePacket(byte[] id, byte[] length, byte[] data) {
        this.id = id;
        this.length = length;
        this.data = data;
    }
}
