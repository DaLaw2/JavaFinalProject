package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;
import java.nio.file.Path;

public class Config implements Serializable {
    public int acceptPort = 8080;
    public int internalTimestamp = 10;
    public int timeoutDuration = 15;
    public int retryDuration = 15;
    public String savePath = Path.of(".", "Downloads").toString();

    public Config() {
    }

    public Config(int acceptPort, int internalTimestamp, int timeoutDuration, int retryDuration, Path savePath) {
        this.acceptPort = acceptPort;
        this.internalTimestamp = internalTimestamp;
        this.timeoutDuration = timeoutDuration;
        this.retryDuration = retryDuration;
        this.savePath = savePath.toString();
    }

    public static Config defaultConfig() {
        return new Config();
    }

    public static boolean validateConfig(Config config) {
        if (!validatePort(config.acceptPort))
            return false;
        if (!validateMiniSecond(config.internalTimestamp))
            return false;
        if (!validateSecond(config.timeoutDuration))
            return false;
        return validateSecond(config.retryDuration);
    }

    private static boolean validatePort(int port) {
        return port >= 1 && port <= 65535;
    }

    private static boolean validateMiniSecond(int timestamp) {
        return timestamp >= 1 && timestamp <= 60000;
    }

    private static boolean validateSecond(int duration) {
        return duration >= 1 && duration <= 3600;
    }
}
