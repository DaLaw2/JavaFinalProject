package DaLaw2.FinalProject.Connection.Utils;

import DaLaw2.FinalProject.Connection.Packet.BasePacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketStream {
    private final Socket socket;

    SocketStream(Socket socket) {
        this.socket = socket;
    }

    public void sendPacket(BasePacket packet) throws IOException {
        OutputStream stream = socket.getOutputStream();
        stream.write(packet.length);
        stream.write(packet.id);
        stream.write(packet.data);
        stream.flush();
    }

    public BasePacket receivePacket() throws IOException {
        InputStream stream = socket.getInputStream();
        byte[] lengthBytes = new byte[8];
        if (stream.read(lengthBytes) != 8) {
            throw new IOException("Failed to read the packet length");
        }

        long length = ByteBuffer.wrap(lengthBytes).getLong();
        byte[] idBytes = new byte[4];
        if (stream.read(idBytes) != 4) {
            throw new IOException("Failed to read the packet ID");
        }

        int dataLength = (int) (length - 12);
        byte[] dataBytes = new byte[dataLength];

        int totalRead = 0;
        while (totalRead < dataLength) {
            int bytesRead = stream.read(dataBytes, totalRead, dataLength - totalRead);
            if (bytesRead == -1) {
                throw new IOException("Failed to read the complete packet data");
            }
            totalRead += bytesRead;
        }

        return new BasePacket(lengthBytes, idBytes, dataBytes);
    }
}
