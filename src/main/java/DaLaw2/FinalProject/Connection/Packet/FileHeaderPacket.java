package DaLaw2.FinalProject.Connection.Packet;

public class FileHeaderPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileHeaderPacket;

    public FileHeaderPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        this.data = data;
    }
}
