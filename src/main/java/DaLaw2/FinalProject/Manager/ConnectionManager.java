package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Connection.Connection;
import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.net.Socket;
import java.util.UUID;
import java.util.HashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager extends Thread {
    private static volatile ConnectionManager instance;
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private ServerSocket serverSocket;
    private HashMap<UUID, Connection> connection;

    private ConnectionManager() {
        Config config;
        while (true) {
            config = ConfigManager.getConfig();
            try {
                this.serverSocket = new ServerSocket(config.acceptPort);
                this.connection = new HashMap<>();
                break;
            } catch (IOException _) {
                try {
                    Thread.sleep(config.retryDuration * 1000L);
                } catch (InterruptedException _) {
                }
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
        Config config;
        ConnectionManager instance = ConnectionManager.getInstance();
        while (true) {
            rwLock.writeLock().lock();
            try {
                instance.serverSocket.setSoTimeout(5000);
                Socket socket = instance.serverSocket.accept();
            } catch (IOException _) {
                continue;
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }
}
