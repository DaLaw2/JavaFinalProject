package DaLaw2.FinalProject.View;

import DaLaw2.FinalProject.Manager.ConfigManager;
import DaLaw2.FinalProject.Manager.ConnectionManager;
import DaLaw2.FinalProject.Manager.DataClass.Config;
import DaLaw2.FinalProject.Manager.DataClass.Task;
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
import java.util.HashMap;
import java.util.UUID;

public class MainFrame extends JFrame {
    private static final Logger logger = LogManager.getLogger(MainFrame.class);

    private static File selectedFile = null;

    public MainFrame() {
        setTitle("檔案傳輸器");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                onWindowClosing();
                System.exit(0);
            }
        });
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
            logger.error("Failed to load custom font: {}", e.getMessage());
        }
        return customFont;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(900, 50));
        topPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

        JLabel hostLabel = new JLabel("主機位置:");
        JLabel portLabel = new JLabel("埠號:");

        Font customFont = loadCustomFont();
        assert customFont != null;

        hostLabel.setFont(customFont);
        portLabel.setFont(customFont);

        RoundJTextField hostField = new RoundJTextField(10);
        RoundJTextField portField = new RoundJTextField(5);
        RoundJButton selectFileButton = new RoundJButton("選擇檔案");
        RoundJButton startTransferButton = new RoundJButton("開始傳輸");

        hostField.setFont(customFont);
        portField.setFont(customFont);
        selectFileButton.setFont(customFont);
        startTransferButton.setFont(customFont);

        selectFileButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION)
                selectedFile = fileChooser.getSelectedFile();
        });

        startTransferButton.addActionListener(_ -> {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                if (host.isEmpty() || port == 0 || port > 65535) {
                    logger.warn("Invalid host or port.");
                    JOptionPane.showMessageDialog(this, "請輸入有效的主機位置和埠號。", "錯誤", JOptionPane.ERROR_MESSAGE);
                } else if (selectedFile == null) {
                    logger.warn("No file selected for transfer.");
                    JOptionPane.showMessageDialog(this, "請先選擇檔案。", "錯誤", JOptionPane.ERROR_MESSAGE);
                } else {
                    Path sourcePath = Paths.get(selectedFile.getAbsolutePath());
                    TaskManager.getInstance().createSendTask(host, port, sourcePath);
                    logger.info("Starting file transfer for: {}", selectedFile.getAbsolutePath());
                }
            } catch (NumberFormatException _) {
                logger.error("Invalid port number.");
                JOptionPane.showMessageDialog(this, "請輸入有效的主機位置和埠號。", "錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                logger.error("Failed to start transfer: {}", e.getMessage());
                JOptionPane.showMessageDialog(this, "無法開始傳輸。", "錯誤", JOptionPane.ERROR_MESSAGE);
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

        gbc.gridx = 6;
        gbc.insets = new Insets(5, 5, 5, 5);

        URL settingsIconURL = MainFrame.class.getResource("/setting.png");
        assert settingsIconURL != null;
        ImageIcon settingsIcon = new ImageIcon(settingsIconURL);
        Image image = settingsIcon.getImage();
        Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
        settingsIcon = new ImageIcon(newimg);
        JButton settingsButton = new JButton(settingsIcon);
        settingsButton.setPreferredSize(new Dimension(30, 30));
        settingsButton.addActionListener(_ -> showConfigDialog());
        topPanel.add(settingsButton, gbc);

        return topPanel;
    }

    private JPanel createTaskPanel() {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBorder(new LineBorder(Color.BLACK, 1, true));

        DefaultListModel<String> taskListModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(taskListModel);
        JScrollPane taskScrollPane = new JScrollPane(taskList);

        Font taskFont = new Font("Time New Roman", Font.PLAIN, 18);
        taskList.setFont(taskFont);

        taskPanel.add(taskScrollPane, BorderLayout.CENTER);

        new Thread(() -> updateTaskList(taskListModel)).start();

        return taskPanel;
    }

    private void updateTaskList(DefaultListModel<String> taskListModel) {
        while (true) {
            HashMap<UUID, Task> currentTasks = TaskManager.getInstance().getTasks();
            SwingUtilities.invokeLater(() -> {
                boolean listChanged = false;
                if (taskListModel.getSize() != currentTasks.size()) {
                    listChanged = true;
                } else {
                    int index = 0;
                    for (Task task : currentTasks.values()) {
                        String taskInfo = String.format("Host: %s, Port: %d, File: %s, Type: %s, Status: %s",
                                task.host, task.port, task.fileName, task.type, task.status);
                        if (!taskInfo.equals(taskListModel.get(index))) {
                            listChanged = true;
                            break;
                        }
                        index++;
                    }
                }
                if (listChanged) {
                    taskListModel.clear();
                    for (Task task : currentTasks.values()) {
                        String taskInfo = String.format("Host: %s, Port: %d, File: %s, Type: %s, Status: %s",
                                task.host, task.port, task.fileName, task.type, task.status);
                        taskListModel.addElement(taskInfo);
                    }
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Failed to update task list: {}", e.getMessage());
            }
        }
    }

    private void showConfigDialog() {
        Config currentConfig = ConfigManager.getConfig();

        JTextField acceptPortField = new JTextField(String.valueOf(currentConfig.acceptPort));
        JTextField internalTimestampField = new JTextField(String.valueOf(currentConfig.internalTimestamp));
        JTextField timeoutDurationField = new JTextField(String.valueOf(currentConfig.timeoutDuration));
        JTextField retryDurationField = new JTextField(String.valueOf(currentConfig.retryDuration));
        JTextField savePathField = new JTextField(currentConfig.savePath);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("綁定埠號(重開後生效):"));
        panel.add(acceptPortField);
        panel.add(new JLabel("內部時間戳(豪秒):"));
        panel.add(internalTimestampField);
        panel.add(new JLabel("超時時間(秒):"));
        panel.add(timeoutDurationField);
        panel.add(new JLabel("重試間隔(秒):"));
        panel.add(retryDurationField);
        panel.add(new JLabel("儲存路徑:"));
        panel.add(savePathField);

        int result = JOptionPane.showConfirmDialog(this, panel, "設定", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int acceptPort = Integer.parseInt(acceptPortField.getText());
                int internalTimestamp = Integer.parseInt(internalTimestampField.getText());
                int timeoutDuration = Integer.parseInt(timeoutDurationField.getText());
                int retryDuration = Integer.parseInt(retryDurationField.getText());
                Path savePath = Path.of(savePathField.getText());
                Config newConfig = new Config(acceptPort, internalTimestamp, timeoutDuration, retryDuration, savePath);
                if (Config.validateConfig(newConfig))
                    ConfigManager.setConfig(newConfig);
                else
                    JOptionPane.showMessageDialog(this, "無效的配置值。", "錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "無效的配置值。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void onWindowClosing() {
        removeWindowListener(getWindowListeners()[0]);
        try {
            TaskManager.dumpToFile();
            ConfigManager.dumpToFile();
            ConnectionManager.getInstance().shutdown();
        } catch (IOException e) {
            logger.error("Failed to dump data to file: {}", e.getMessage());
        }
    }
}
