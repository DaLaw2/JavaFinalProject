package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Connection.IncomingConnection;
import DaLaw2.FinalProject.Connection.OutgoingConnection;
import DaLaw2.FinalProject.Connection.Utils.SocketStream;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager extends Thread {
    private static final ConnectionManager instance = new ConnectionManager();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    private ServerSocket serverSocket;
    private final HashMap<UUID, IncomingConnection> incomingConnections = new HashMap<>();
    private final HashMap<UUID, OutgoingConnection> outgoingConnections = new HashMap<>();

    private ConnectionManager() {
        Config config;
        while (true) {
            config = ConfigManager.getConfig();
            try {
                this.serverSocket = new ServerSocket(config.acceptPort);
                break;
            } catch (IOException _) {
                logger.error("Failed to bind to port {}.", config.acceptPort);
                try {
                    Thread.sleep(config.retryDuration * 1000L);
                } catch (InterruptedException _) {
                    logger.error("Thread was interrupted during sleep.");
                }
            }
        }
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            acceptConnection();
        }
        logger.info("Connection Manager Terminate Complete.");
    }

    public void shutdown() {
        logger.info("Connection Manager Terminating.");
        interrupt();
        try {
            serverSocket.close();
        } catch (IOException _) {
            logger.error("Failed to close server socket.");
        }
    }

    private void acceptConnection() {
        Config config = ConfigManager.getConfig();
        rwLock.readLock().lock();
        try {
            serverSocket.setSoTimeout(config.internalTimestamp);
            Socket socket = serverSocket.accept();
            SocketStream socketStream = new SocketStream(socket);
            IncomingConnection incomingConnection = new IncomingConnection(socketStream);
            UUID uuid = incomingConnection.getUUID();
            incomingConnection.start();
            this.incomingConnections.put(uuid, incomingConnection);
            logger.info("Accepted new connection: {}", uuid);
        } catch (IOException _) {
            logger.error("Failed to accept connection");
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void connectTo(String host, int port) {
        rwLock.readLock().lock();
        try {
            Socket socket = new Socket(host, port);
            UUID uuid = UUID.randomUUID();
            SocketStream socketStream = new SocketStream(socket);
            OutgoingConnection outgoingConnection = new OutgoingConnection(uuid, socketStream);
            outgoingConnection.start();
            this.outgoingConnections.put(uuid, outgoingConnection);
            logger.info("Connected to {}:{}.", host, port);
        } catch (IOException _) {
            logger.error("Failed to connect to {}:{}.", host, port);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
