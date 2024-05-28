package DaLaw2.FinalProject.Connection.Packet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public enum PacketType {
    BasePacket(0),
    FileBodyPacket(1),
    FileEndPacket(2),
    FileHeaderPacket(3),
    FileRequireResendPacket(4),
    FileTransferAcknowledgePacket(5),
    HeartPacket(6);

    private final int id;
    private static final Logger logger = LogManager.getLogger(PacketType.class);
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
            if (packetType == null) {
                logger.error("No PacketType found.");
                throw new ClassCastException("Invalid PacketType ID.");
            }
            return packetType;
        } catch (BufferOverflowException _) {
            throw new ClassCastException("Invalid byte array length.");
        }
    }
}
