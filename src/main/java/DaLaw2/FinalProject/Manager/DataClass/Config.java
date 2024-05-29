package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;

public class Config implements Serializable {
    public int acceptPort = 8080;
    public int startPort = 60000;
    public int endPort = 65535;
    public int internalTimestamp = 10;
    public int timeoutDuration = 15;
    public int retryDuration = 15;

    private Config() {
    }

    public Config(int acceptPort, int startPort, int endPort, int internalTimestamp, int timeoutDuration, int retryDuration) {
        this.acceptPort = acceptPort;
        this.startPort = startPort;
        this.endPort = endPort;
        this.internalTimestamp = internalTimestamp;
        this.timeoutDuration = timeoutDuration;
        this.retryDuration = retryDuration;
    }

    public static Config defaultConfig() {
        return new Config();
    }

    public static boolean validateConfig(Config config) {
        if (!validatePort(config.acceptPort))
            return false;
        if (!validatePortRange(config.startPort, config.endPort))
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

    private static boolean validatePortRange(int startPort, int endPort) {
        return startPort >= 1 && startPort <= 65535 && endPort >= 1 && endPort <= 65535;
    }

    private static boolean validateMiniSecond(int timestamp) {
        return timestamp >= 1 && timestamp <= 60000;
    }

    private static boolean validateSecond(int duration) {
        return duration >= 1 && duration <= 3600;
    }
}
