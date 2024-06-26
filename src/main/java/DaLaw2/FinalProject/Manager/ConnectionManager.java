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
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager extends Thread {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    private static final ConnectionManager instance = new ConnectionManager();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

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
            } catch (IOException e) {
                logger.error("Failed to bind to port {}.", config.acceptPort, e);
                try {
                    Thread.sleep(config.retryDuration * 1000L);
                } catch (InterruptedException _) {
                }
            }
        }
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted())
            acceptConnection();
        logger.info("Connection Manager Terminate Complete.");
    }

    public void shutdown() {
        logger.info("Connection Manager Terminating.");
        interrupt();
        try {
            serverSocket.close();
            for (IncomingConnection incomingConnection : incomingConnections.values())
                incomingConnection.shutdown();
            for (OutgoingConnection outgoingConnection : outgoingConnections.values())
                outgoingConnection.shutdown();
        } catch (IOException e) {
            logger.error("Failed to close server socket: {}", e.getMessage());
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
            String host = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            String fileName = incomingConnection.getFileName();
            Path filePath = Path.of(config.savePath, fileName);
            TaskManager.getInstance().createReceiveTask(uuid, host, port, filePath);
            this.incomingConnections.put(uuid, incomingConnection);
            incomingConnection.start();
            logger.info("Accepted new connection: {}", uuid);
        } catch (SocketTimeoutException e) {
            try {
                Thread.sleep(config.retryDuration);
            } catch (InterruptedException _) {
            }
        } catch (Exception e) {
            logger.error("Failed to accept connection: {}", e.getMessage());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void startConnection(UUID uuid, String host, int port, Path sourcePath) {
        rwLock.readLock().lock();
        try {
            Socket socket = new Socket(host, port);
            SocketStream socketStream = new SocketStream(socket);
            OutgoingConnection outgoingConnection = new OutgoingConnection(uuid, socketStream, sourcePath);
            this.outgoingConnections.put(uuid, outgoingConnection);
            outgoingConnection.start();
            logger.info("Started new connection: {}", uuid);
        } catch (Exception e) {
            logger.error("Failed to start connection: {}", e.getMessage());
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
