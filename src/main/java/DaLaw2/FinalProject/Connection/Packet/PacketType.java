package DaLaw2.FinalProject.Connection.Packet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

public enum PacketType {
    BasePacket(0),
    FileHeaderPacket(3),
    FileBodyPacket(1),
    FileRequireSendPacket(4),
    EndTransferPacket(2);

    private final int id;
    private static final HashMap<Integer, PacketType> idToPacketTypeMap = new HashMap<>();

    static {
        for (PacketType packetType : values()) {
            idToPacketTypeMap.put(packetType.id, packetType);
        }
    }

    PacketType(int id) {
        this.id = id;
    }

    public byte[] toByte() {
        return ByteBuffer.allocate(4).putInt(id).array();
    }

    public static PacketType fromByte(byte[] bytes) throws ClassCastException {
        try {
            int id = ByteBuffer.wrap(bytes).getInt();
            PacketType packetType = idToPacketTypeMap.get(id);
            if (packetType == null)
                throw new ClassCastException("Invalid PacketType ID.");
            return packetType;
        } catch (BufferOverflowException _) {
            throw new ClassCastException("Invalid byte array length.");
        }
    }
}
