package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Connection.Connection;
import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.util.UUID;
import java.util.HashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager {
    private static volatile ConnectionManager instance;
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private ServerSocket serverSocket;
    private HashMap<UUID, Connection> connection;

    private ConnectionManager() {
        while (true) {
            try {
                Config config = ConfigManager.getConfig();
                this.serverSocket = new ServerSocket(config.acceptPort);
                this.connection = new HashMap<>();
                break;
            } catch (IOException e) {
                continue;
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

    public static void acceptConnection() {

    }
}
