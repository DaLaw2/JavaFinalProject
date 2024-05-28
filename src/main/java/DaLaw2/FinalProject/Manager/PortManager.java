package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PortManager {
    private static volatile PortManager instance;
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ConcurrentSkipListSet<Integer> availablePorts = new ConcurrentSkipListSet<>();

    private PortManager() {
        Config config = ConfigManager.getConfig();
        for (int port = config.startPort; port <= config.endPort; port++)
            availablePorts.add(port);
    }

    public static PortManager getInstance() {
        if (instance == null) {
            rwLock.writeLock().lock();
            try {
                if (instance == null) {
                    instance = new PortManager();
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        return instance;
    }

    public static Integer allocatePort() {
        PortManager instance = PortManager.getInstance();
        rwLock.writeLock().lock();
        try {
            return instance.availablePorts.pollFirst();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void freePort(int port) {
        PortManager instance = PortManager.getInstance();
        rwLock.writeLock().lock();
        try {
            Config config = ConfigManager.getConfig();
            if (port >= config.startPort && port <= config.endPort)
                instance.availablePorts.add(port);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
