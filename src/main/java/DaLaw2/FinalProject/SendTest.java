package DaLaw2.FinalProject;

import DaLaw2.FinalProject.Manager.ConnectionManager;
import DaLaw2.FinalProject.Manager.TaskManager;

import java.io.IOException;
import java.nio.file.Path;

public class SendTest {
    public static void main(String[] args) throws IOException {
        ConnectionManager.getInstance().start();
        TaskManager.getInstance().createSendTask("0.0.0.0", 9090, Path.of("D:\\ISO\\Ubuntu 24.04 Destop.iso"));
    }
}
