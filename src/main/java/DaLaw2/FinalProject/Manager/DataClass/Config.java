package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;

public class Config implements Serializable {
    public int acceptPort;
    public int startPort;
    public int endPort;
    public int timeoutDuration;

    public Config(int acceptPort, int startPort, int endPort) {
        this.acceptPort = acceptPort;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeoutDuration = 15;
    }

    public static Config defaultConfig() {
        return new Config(8080, 60000, 65535);
    }

    public static boolean validateConfig(Config config) {
        if (config.acceptPort < 1 || config.acceptPort > 65535)
            return false;
        if (config.startPort < 1 || config.startPort > config.endPort || config.endPort > 65535)
            return false;
        if (config.timeoutDuration < 1 || config.timeoutDuration > 3600)
            return false;
        return true;
    }
}