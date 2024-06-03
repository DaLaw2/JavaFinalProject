package DaLaw2.FinalProject.Connection.Packet;

import DaLaw2.FinalProject.Utils.Convertor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FileRequireSendPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileRequireSendPacket;

    public FileRequireSendPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public FileRequireSendPacket(ArrayList<Long> requireSend) {
        super();
        this.id = packetType.toByte();
        byte[] data = Convertor.arrayListToByteArray(requireSend);
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public static FileRequireSendPacket fromBasePacket(BasePacket packet) {
        return new FileRequireSendPacket(packet.data);
    }

    public ArrayList<Long> unwarp() {
        return Convertor.arrayListFromByteArray(data);
    }
}
