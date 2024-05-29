package DaLaw2.FinalProject.Connection;

import DaLaw2.FinalProject.Connection.Utils.SocketStream;

import java.util.UUID;

public class IncomingConnection extends Thread {
    private final UUID uuid;
    private final SocketStream socket;

    public IncomingConnection(SocketStream socket) {
        this.socket = socket;

    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void run() {

    }
}
