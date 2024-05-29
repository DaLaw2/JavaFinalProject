package DaLaw2.FinalProject.Manager.Utils;

import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PortPool {
    private static volatile PortPool instance = new PortPool();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ConcurrentSkipListSet<Integer> availablePorts = new ConcurrentSkipListSet<>();

    private PortPool() {
        Config config = ConfigManager.getConfig();
        for (int port = config.startPort; port <= config.endPort; port++)
            availablePorts.add(port);
    }

    public static PortPool getInstance() {
        return instance;
    }

    public static Integer allocatePort() {
        PortPool instance = PortPool.getInstance();
        rwLock.writeLock().lock();
        try {
            return instance.availablePorts.pollFirst();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void freePort(int port) {
        PortPool instance = PortPool.getInstance();
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
