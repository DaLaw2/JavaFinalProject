package DaLaw2.FinalProject;

import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.ConnectionManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;

import java.io.IOException;

public class ReceiveTest {
    public static void main(String[] args) throws IOException {
        Config config = ConfigManager.getConfig();
        config.acceptPort = 9090;
        ConfigManager.setConfig(config);
        ConnectionManager.getInstance().start();
    }
}
