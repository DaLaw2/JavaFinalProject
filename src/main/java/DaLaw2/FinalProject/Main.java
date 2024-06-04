package DaLaw2.FinalProject;

import DaLaw2.FinalProject.Manager.ConnectionManager;
import DaLaw2.FinalProject.View.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
        ConnectionManager.getInstance().start();
    }

    private static void createAndShowGUI() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}