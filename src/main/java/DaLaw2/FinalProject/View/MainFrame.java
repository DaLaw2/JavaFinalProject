package DaLaw2.FinalProject.View;

import DaLaw2.FinalProject.Manager.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainFrame extends JFrame {
    private static final Logger logger = LogManager.getLogger(MainFrame.class);
    private static File selectedFile = null;

    public MainFrame() {
        setTitle("檔案傳輸器");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL iconURL = MainFrame.class.getResource("/icon.png");
        assert iconURL != null;
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        setResizable(false);
        setExtendedState(JFrame.NORMAL);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = createTopPanel();
        JPanel taskPanel = createTaskPanel();

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(taskPanel, BorderLayout.CENTER);

        add(panel);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(900, 50));
        topPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

        Font customFont = loadCustomFont();

        JLabel hostLabel = new JLabel("主機位置:");
        JLabel portLabel = new JLabel("埠號:");
        if (customFont != null) {
            hostLabel.setFont(customFont);
            portLabel.setFont(customFont);
        }

        RoundJTextField hostField = new RoundJTextField(10);
        RoundJTextField portField = new RoundJTextField(5);
        RoundJButton selectFileButton = new RoundJButton("選擇檔案");
        RoundJButton startTransferButton = new RoundJButton("開始傳輸");

        if (customFont != null) {
            hostField.setFont(customFont);
            portField.setFont(customFont);
            selectFileButton.setFont(customFont);
            startTransferButton.setFont(customFont);
        }

        selectFileButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                logger.info("Selected file: {}", selectedFile.getAbsolutePath());
            }
        });

        startTransferButton.addActionListener(_ -> {
            if (selectedFile != null) {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                Path sourcePath = Paths.get(selectedFile.getAbsolutePath());
                TaskManager.getInstance().createSendTask(host, port, sourcePath);
                logger.info("Starting file transfer for: {}", selectedFile.getAbsolutePath());
            } else {
                logger.warn("No file selected for transfer.");
                JOptionPane.showMessageDialog(this, "請先選擇檔案。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(hostLabel, gbc);

        gbc.gridx = 1;
        topPanel.add(hostField, gbc);

        gbc.gridx = 2;
        topPanel.add(portLabel, gbc);

        gbc.gridx = 3;
        topPanel.add(portField, gbc);

        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        selectFileButton.setPreferredSize(new Dimension(120, 30));
        topPanel.add(selectFileButton, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(5, 30, 5, 5);
        startTransferButton.setPreferredSize(new Dimension(120, 30));
        topPanel.add(startTransferButton, gbc);

        return topPanel;
    }

    private JPanel createTaskPanel() {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

        DefaultListModel<String> taskListModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(taskListModel);
        JScrollPane taskScrollPane = new JScrollPane(taskList);

        taskPanel.add(taskScrollPane, BorderLayout.CENTER);

        new Thread(() -> updateTaskList(taskListModel)).start();

        return taskPanel;
    }

    private void updateTaskList(DefaultListModel<String> taskListModel) {
        while (true) {
            taskListModel.clear();
            TaskManager.getInstance().getTasks().forEach((_, task) -> {
                String taskInfo = String.format("Host: %s, Port: %d, File: %s, Type: %s, Status: %s", task.host, task.port, task.fileName, task.type, task.status);
                taskListModel.addElement(taskInfo);
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Failed to update task list", e);
            }
        }
    }

    private Font loadCustomFont() {
        Font customFont = null;
        try {
            InputStream is = MainFrame.class.getResourceAsStream("/font.ttf");
            if (is != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
            }
        } catch (IOException | FontFormatException e) {
            logger.error("Failed to load custom font", e);
        }
        return customFont;
    }
}