package DaLaw2.FinalProject.Manager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import DaLaw2.FinalProject.Connection.Receiver;
import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.util.UUID;
import java.net.Socket;
import java.util.HashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager extends Thread {
    private static volatile ConnectionManager instance;
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private ServerSocket serverSocket;
    private final HashMap<UUID, Receiver> connections = new HashMap<>();

    private ConnectionManager() {
        Config config;
        while (true) {
            config = ConfigManager.getConfig();
            try {
                this.serverSocket = new ServerSocket(config.acceptPort);
                break;
            } catch (IOException _) {
                logger.error("Failed to bind to port {}", config.acceptPort);
                try {
                    Thread.sleep(config.retryDuration * 1000L);
                } catch (InterruptedException _) {
                    logger.error("Thread was interrupted during sleep.");
                }
            }
        }
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            rwLock.writeLock().lock();
            try {
                if (instance == null) {
                    instance = new ConnectionManager();
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
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
        } catch (IOException e) {
            logger.error("Failed to close server socket", e);
        }
    }

    private void acceptConnection() {
        Config config = ConfigManager.getConfig();
        rwLock.readLock().lock();
        try {
            serverSocket.setSoTimeout(config.internalTimestamp);
            Socket socket = serverSocket.accept();
            Receiver receiver = new Receiver(socket);
            UUID uuid = receiver.getId();
            receiver.start();
            connections.put(uuid, receiver);
            logger.info("Accepted new connection: {}", uuid);
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                logger.error("Failed to accept connection", e);
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
