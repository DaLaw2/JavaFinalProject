package DaLaw2.FinalProject.Connection;

import java.net.Socket;
import java.util.UUID;

public class Receiver extends Thread {
    private final UUID id;
    private final Socket socket;

    public Receiver(Socket socket) {
        this.socket = socket;

    }
}
