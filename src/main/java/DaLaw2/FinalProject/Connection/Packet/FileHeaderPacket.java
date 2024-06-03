package DaLaw2.FinalProject.Connection.Packet;

import DaLaw2.FinalProject.Manager.DataClass.FileHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class FileHeaderPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileHeaderPacket;

    public FileHeaderPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public FileHeaderPacket(FileHeader fileHeader) throws IOException {
        super();
        this.id = packetType.toByte();
        byte[] data = fileHeader.toByteArray();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public static FileHeaderPacket fromBasePacket(BasePacket basePacket) {
        return new FileHeaderPacket(basePacket.data);
    }

    public FileHeader unwarp() throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(stream);
        return (FileHeader) in.readObject();
    }
}
