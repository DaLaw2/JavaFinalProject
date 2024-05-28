package DaLaw2.FinalProject.Connection.Packet;

public class BasePacket {
    public byte[] length;
    public byte[] id;
    public byte[] data;
    public final PacketType packetType = PacketType.BasePacket;

    public BasePacket() {
    }


}
