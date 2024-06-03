package DaLaw2.FinalProject.Connection.Packet;

import DaLaw2.FinalProject.Manager.DataClass.FileBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class FileBodyPacket extends BasePacket {
    public final PacketType packetType = PacketType.FileBodyPacket;

    public FileBodyPacket(byte[] data) {
        super();
        this.id = packetType.toByte();
        long length = 12 + data.length;
        this.length = ByteBuffer.allocate(8).putLong(length).array();
        this.data = data;
    }

    public static FileBodyPacket fromBasePacket(BasePacket basePacket) {
        return new FileBodyPacket(basePacket.data);
    }

    public FileBody unwarp() throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(stream);
        return (FileBody) in.readObject();
    }
}
