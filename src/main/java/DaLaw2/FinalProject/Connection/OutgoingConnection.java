package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Utils.SocketStream;

import java.util.UUID;

public class OutgoingConnection extends Thread {
    private final UUID uuid;
    private final SocketStream socket;

    public OutgoingConnection(SocketStream socket) {
        this.socket = socket;
        this.uuid = UUID.randomUUID();
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void run() {

    }
}