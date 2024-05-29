package DaLaw2.FinalProject.Manager;

import DaLaw2.FinalProject.Main;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigManager {
    private static volatile ConfigManager instance;
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final Config config;

    private ConfigManager() {
        Optional<Config> parseFromFile = tryParseFromFile();
        this.config = parseFromFile.orElseGet(Config::defaultConfig);
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            rwLock.writeLock().lock();
            try {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        return instance;
    }

    public static Config getConfig() {
        ConfigManager instance = ConfigManager.getInstance();
        rwLock.readLock().lock();
        try {
            return instance.config;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public static void setConfig(Config config) throws IOException, IllegalArgumentException {
        if (!Config.validateConfig(config)) {
            throw new IllegalArgumentException("Invalid config");
        }
        dumpToFile(config);
    }

    private static Optional<Config> tryParseFromFile() {
        try {
            String fileName = ".config";
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            return Optional.of((Config) in.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException _) {
            logger.info("No config found");
            return Optional.empty();
        }
    }

    private static void dumpToFile(Config config) throws IOException {
        String fileName = ".config";
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(config);
    }
}