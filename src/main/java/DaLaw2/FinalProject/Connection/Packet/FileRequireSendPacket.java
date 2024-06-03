package DaLaw2.FinalProject.Connection.Packet;

import DaLaw2.FinalProject.Utils.Convertor;

import java.util.ArrayList;

public class FileRequireSendPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileRequireSendPacket;

    public FileRequireSendPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        this.data = data;
    }

    public FileRequireSendPacket(ArrayList<Long> requireSend) {
        super();
        this.id = packetType.toByte();
        this.data = Convertor.arrayListToByteArray(requireSend);
    }

    public static FileRequireSendPacket fromBasePacket(BasePacket packet) {
        return new FileRequireSendPacket(packet.data);
    }

    public ArrayList<Long> unwarp() {
        return Convertor.arrayListFromByteArray(data);
    }
}
