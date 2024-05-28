package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;

public class Config implements Serializable {
    public int acceptPort;
    public int startPort;
    public int endPort;
    public int internalTimestamp;
    public int timeoutDuration;
    public int retryDuration;

    public Config(int acceptPort, int startPort, int endPort, int internalTimestamp, int timeoutDuration, int retryDuration) {
        this.acceptPort = acceptPort;
        this.startPort = startPort;
        this.endPort = endPort;
        this.internalTimestamp = internalTimestamp;
        this.timeoutDuration = timeoutDuration;
        this.retryDuration = retryDuration;
    }

    public static Config defaultConfig() {
        return new Config(8080, 60000, 65535, 10, 15, 15);
    }

    public static boolean validateConfig(Config config) {
        if (config.acceptPort < 1 || config.acceptPort > 65535)
            return false;
        if (config.startPort < 1 || config.startPort > config.endPort || config.endPort > 65535)
            return false;
        if (config.internalTimestamp < 1 || config.internalTimestamp > 60000)
            return false;
        if (config.timeoutDuration < 1 || config.timeoutDuration > 3600)
            return false;
        if (config.retryDuration < 1 || config.retryDuration > 3600)
            return false;
        return true;
    }
}