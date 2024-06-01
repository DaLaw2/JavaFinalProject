package DaLaw2.FinalProject.Connection.Packet;

import DaLaw2.FinalProject.Utils.Convertor;

import java.util.ArrayList;

public class FileRequireSendPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileRequireSendPacket;

    public FileRequireSendPacket(ArrayList<Long> requireSend) {
        super();
        this.id = packetType.toByte();
        this.data = Convertor.arrayListToByteArray(requireSend);
    }
}
