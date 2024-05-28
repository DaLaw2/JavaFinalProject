package DaLaw2.FinalProject.Connection;

import java.net.Socket;
import java.util.UUID;

public class Connection {
    private final UUID id;
    private final Socket socket;

    public Connection(UUID id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }
}
