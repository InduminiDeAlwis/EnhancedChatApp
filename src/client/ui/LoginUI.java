package client.ui;

import client.Client;
import client.utils.UIUtils;
import common.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginUI.java
 * Login window where users enter their username and connect to server
 */
public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JLabel statusLabel;

    private Client client;

    /**
     * Constructor
     */
    public LoginUI() {
        initializeUI();
        this.client = new Client();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        setTitle("Enhanced Chat - Login");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Welcome to Enhanced Chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Connect to start chatting");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(UIUtils.TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameField = UIUtils.createTextField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Host field
        JLabel hostLabel = new JLabel("Server Host:");
        hostLabel.setFont(new Font("Arial", Font.BOLD, 12));
        hostField = UIUtils.createTextField("localhost");
        hostField.setText(Constants.SERVER_HOST);
        hostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Port field
        JLabel portLabel = new JLabel("Server Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 12));
        portField = UIUtils.createTextField(String.valueOf(Constants.SERVER_PORT));
        portField.setText(String.valueOf(Constants.SERVER_PORT));
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Connect button
        connectButton = UIUtils.createButton("Connect", UIUtils.PRIMARY_COLOR);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setMaximumSize(new Dimension(200, 40));
        connectButton.addActionListener(e -> handleConnect());

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(UIUtils.DANGER_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        mainPanel.add(titleLabel);
        mainPanel.add(UIUtils.createVerticalSpacer(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(UIUtils.createVerticalSpacer(30));

        mainPanel.add(usernameLabel);
        mainPanel.add(UIUtils.createVerticalSpacer(5));
        mainPanel.add(usernameField);
        mainPanel.add(UIUtils.createVerticalSpacer(15));

        mainPanel.add(hostLabel);
        mainPanel.add(UIUtils.createVerticalSpacer(5));
        mainPanel.add(hostField);
        mainPanel.add(UIUtils.createVerticalSpacer(15));

        mainPanel.add(portLabel);
        mainPanel.add(UIUtils.createVerticalSpacer(5));
        mainPanel.add(portField);
        mainPanel.add(UIUtils.createVerticalSpacer(25));

        mainPanel.add(connectButton);
        mainPanel.add(UIUtils.createVerticalSpacer(10));
        mainPanel.add(statusLabel);

        add(mainPanel, BorderLayout.CENTER);

        // Enter key to connect
        usernameField.addActionListener(e -> handleConnect());
        hostField.addActionListener(e -> handleConnect());
        portField.addActionListener(e -> handleConnect());

        UIUtils.centerWindow(this);
    }

    /**
     * Handle connect button click
     */
    private void handleConnect() {
        String username = usernameField.getText().trim();
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();

        // Validation
        if (username.isEmpty()) {
            showStatus("Please enter a username", true);
            return;
        }

        if (username.length() < 3 || username.length() > 20) {
            showStatus("Username must be 3-20 characters", true);
            return;
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            showStatus("Username can only contain letters, numbers, and underscore", true);
            return;
        }

        if (host.isEmpty()) {
            showStatus("Please enter server host", true);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1024 || port > 65535) {
                showStatus("Port must be between 1024 and 65535", true);
                return;
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid port number", true);
            return;
        }

        // Disable button during connection
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");
        showStatus("Connecting to server...", false);

        // Connect in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                // Connect to server
                boolean connected = client.connect(host, port);

                if (!connected) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Failed to connect to server", true);
                        connectButton.setEnabled(true);
                        connectButton.setText("Connect");
                    });
                    return;
                }

                // Login with username
                boolean loggedIn = client.login(username);

                if (loggedIn) {
                    // Success! Open chat window
                    SwingUtilities.invokeLater(() -> {
                        openChatWindow();
                    });
                } else {
                    // Login failed (username taken or error)
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Login failed. Username may be taken.", true);
                        connectButton.setEnabled(true);
                        connectButton.setText("Connect");
                        client.disconnect();
                    });
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("Error: " + e.getMessage(), true);
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Show status message
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? UIUtils.DANGER_COLOR : UIUtils.SECONDARY_COLOR);
    }

    /**
     * Open chat window after successful login
     */
    private void openChatWindow() {
        ChatClientUI chatUI = new ChatClientUI(client);
        chatUI.setVisible(true);
        this.dispose(); // Close login window
    }

    /**
     * Main method - Entry point for client application
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        // Create and show login window
        SwingUtilities.invokeLater(() -> {
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });
    }
}
