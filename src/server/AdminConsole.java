package server;

import common.Constants;
import common.Message;
import common.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class AdminConsole extends JFrame {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected = false;
    
    // GUI Components
    private JTextArea clientListArea;
    private JTextArea chatHistoryArea;
    private JTextArea systemStatsArea;
    private JTextField kickUserField;
    private JButton connectButton;
    private JButton refreshClientsButton;
    private JButton refreshHistoryButton;
    private JButton refreshStatsButton;
    private JButton kickButton;
    private JLabel statusLabel;
    
    public AdminConsole() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle(Constants.ADMIN_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 245));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Connected Clients Tab
        JPanel clientsPanel = createClientsPanel();
        tabbedPane.addTab("📋 Connected Clients", clientsPanel);
        
        // Chat History Tab
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("💬 Chat History", historyPanel);
        
        // System Statistics Tab
        JPanel statsPanel = createStatsPanel();
        tabbedPane.addTab("📊 System Statistics", statsPanel);
        
        // Admin Actions Tab
        JPanel actionsPanel = createActionsPanel();
        tabbedPane.addTab("⚙️ Admin Actions", actionsPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
        
        // Initially disable all controls except connect button
        setControlsEnabled(false);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("🛡️ Admin Console - Enhanced Chat System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        connectButton = new JButton("Connect to Server");
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.setBackground(new Color(46, 204, 113));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setBorderPainted(false);
        connectButton.setPreferredSize(new Dimension(180, 40));
        connectButton.addActionListener(e -> connectToServer());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(connectButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Controls panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshClientsButton = new JButton("🔄 Refresh");
        refreshClientsButton.addActionListener(e -> requestConnectedClients());
        controlPanel.add(refreshClientsButton);
        
        JLabel infoLabel = new JLabel("💡 Auto-updates when clients join/leave");
        infoLabel.setForeground(Color.GRAY);
        controlPanel.add(infoLabel);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Client list area
        clientListArea = new JTextArea();
        clientListArea.setEditable(false);
        clientListArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        clientListArea.setBackground(Color.WHITE);
        clientListArea.setText("Connect to server to view connected clients...");
        
        JScrollPane scrollPane = new JScrollPane(clientListArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Connected Clients", 
            TitledBorder.LEFT, 
            TitledBorder.TOP));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Controls panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshHistoryButton = new JButton("🔄 Refresh");
        refreshHistoryButton.addActionListener(e -> requestChatHistory());
        controlPanel.add(refreshHistoryButton);
        
        JButton clearButton = new JButton("🗑️ Clear Display");
        clearButton.addActionListener(e -> chatHistoryArea.setText(""));
        controlPanel.add(clearButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Chat history area
        chatHistoryArea = new JTextArea();
        chatHistoryArea.setEditable(false);
        chatHistoryArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        chatHistoryArea.setBackground(Color.WHITE);
        chatHistoryArea.setLineWrap(true);
        chatHistoryArea.setWrapStyleWord(true);
        chatHistoryArea.setText("Connect to server to view chat history...");
        
        JScrollPane scrollPane = new JScrollPane(chatHistoryArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Chat History (Last " + Constants.MAX_HISTORY_SIZE + " messages)", 
            TitledBorder.LEFT, 
            TitledBorder.TOP));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Controls panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshStatsButton = new JButton("🔄 Refresh");
        refreshStatsButton.addActionListener(e -> requestSystemStats());
        controlPanel.add(refreshStatsButton);
        
        JLabel infoLabel = new JLabel("💡 Shows real-time server statistics");
        infoLabel.setForeground(Color.GRAY);
        controlPanel.add(infoLabel);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Stats area
        systemStatsArea = new JTextArea();
        systemStatsArea.setEditable(false);
        systemStatsArea.setFont(new Font("Consolas", Font.BOLD, 16));
        systemStatsArea.setBackground(Color.WHITE);
        systemStatsArea.setText("Connect to server to view statistics...");
        
        JScrollPane scrollPane = new JScrollPane(systemStatsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "System Statistics", 
            TitledBorder.LEFT, 
            TitledBorder.TOP));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Kick user section
        JPanel kickPanel = new JPanel();
        kickPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        kickPanel.setBackground(Color.WHITE);
        kickPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED), 
            "⚠️ Kick User", 
            TitledBorder.LEFT, 
            TitledBorder.TOP));
        kickPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JLabel kickLabel = new JLabel("Username:");
        kickLabel.setFont(new Font("Arial", Font.BOLD, 14));
        kickPanel.add(kickLabel);
        
        kickUserField = new JTextField(20);
        kickUserField.setFont(new Font("Arial", Font.PLAIN, 14));
        kickPanel.add(kickUserField);
        
        kickButton = new JButton("❌ Kick User");
        kickButton.setBackground(new Color(231, 76, 60));
        kickButton.setForeground(Color.WHITE);
        kickButton.setFocusPainted(false);
        kickButton.addActionListener(e -> kickUser());
        kickPanel.add(kickButton);
        
        contentPanel.add(kickPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Instructions
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(236, 240, 241));
        instructionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "ℹ️ Instructions", 
            TitledBorder.LEFT, 
            TitledBorder.TOP));
        
        String[] instructions = {
            "• To kick a user, enter their exact username and click 'Kick User'",
            "• Use the 'Connected Clients' tab to see all active users",
            "• Chat history is limited to " + Constants.MAX_HISTORY_SIZE + " most recent messages",
            "• All tabs auto-refresh when significant events occur",
            "• System statistics show real-time server metrics"
        };
        
        for (String instruction : instructions) {
            JLabel label = new JLabel(instruction);
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            instructionsPanel.add(label);
        }
        
        contentPanel.add(instructionsPanel);
        
        panel.add(contentPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        statusLabel = new JLabel("⚪ Disconnected");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void connectToServer() {
        if (connected) {
            disconnect();
            return;
        }
        
        // Show login dialog
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(Constants.ADMIN_USERNAME);
        panel.add(usernameField);
        
        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(Constants.ADMIN_PASSWORD);
        panel.add(passwordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Login", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            // Connect in background thread
            new Thread(() -> {
                try {
                    socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(socket.getInputStream());
                    
                    // Send admin login
                    String credentials = username + ":" + password;
                    Message loginMsg = new Message(MessageType.ADMIN_LOGIN, "ADMIN", credentials);
                    output.writeObject(loginMsg);
                    output.flush();
                    
                    // Wait for response
                    Message response = (Message) input.readObject();
                    
                    if (MessageType.ADMIN_AUTH_SUCCESS.equals(response.getType())) {
                        connected = true;
                        SwingUtilities.invokeLater(() -> {
                            updateStatus("🟢 Connected to " + Constants.SERVER_HOST + ":" + Constants.SERVER_PORT, true);
                            connectButton.setText("Disconnect");
                            connectButton.setBackground(new Color(231, 76, 60));
                            setControlsEnabled(true);
                            JOptionPane.showMessageDialog(this, "Successfully connected as Admin!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        });
                        
                        // Start listening for server messages
                        listenForServerMessages();
                        
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Invalid admin credentials!", 
                                "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                        });
                        disconnect();
                    }
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Failed to connect to server:\n" + e.getMessage(), 
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                        updateStatus("⚪ Disconnected", false);
                    });
                }
            }).start();
        }
    }
    
    private void listenForServerMessages() {
        new Thread(() -> {
            try {
                while (connected) {
                    Message message = (Message) input.readObject();
                    handleServerMessage(message);
                }
            } catch (Exception e) {
                if (connected) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Connection lost: " + e.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        disconnect();
                    });
                }
            }
        }).start();
    }
    
    private void handleServerMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case MessageType.CONNECTED_CLIENTS_LIST:
                    clientListArea.setText("=== Connected Clients ===\n" + message.getContent() + 
                        "\n=========================");
                    break;
                    
                case MessageType.CHAT_HISTORY_DATA:
                    chatHistoryArea.setText("=== Chat History ===\n" + message.getContent() + 
                        "\n====================");
                    break;
                    
                case MessageType.SYSTEM_STATS_DATA:
                    systemStatsArea.setText(message.getContent());
                    break;
                    
                case MessageType.KICK_SUCCESS:
                case MessageType.KICK_FAILED:
                    JOptionPane.showMessageDialog(this, message.getContent(), 
                        "Kick User", JOptionPane.INFORMATION_MESSAGE);
                    kickUserField.setText("");
                    requestConnectedClients();
                    break;
            }
        });
    }
    
    private void requestConnectedClients() {
        if (!connected) return;
        
        try {
            Message request = new Message(MessageType.GET_CONNECTED_CLIENTS, "ADMIN", "");
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            showError("Failed to request client list: " + e.getMessage());
        }
    }
    
    private void requestChatHistory() {
        if (!connected) return;
        
        try {
            Message request = new Message(MessageType.GET_CHAT_HISTORY, "ADMIN", "");
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            showError("Failed to request chat history: " + e.getMessage());
        }
    }
    
    private void requestSystemStats() {
        if (!connected) return;
        
        try {
            Message request = new Message(MessageType.GET_SYSTEM_STATS, "ADMIN", "");
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            showError("Failed to request system stats: " + e.getMessage());
        }
    }
    
    private void kickUser() {
        if (!connected) return;
        
        String username = kickUserField.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username to kick", 
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to kick user '" + username + "'?", 
            "Confirm Kick", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Message kickMsg = new Message(MessageType.KICK_USER, "ADMIN", username);
                output.writeObject(kickMsg);
                output.flush();
            } catch (IOException e) {
                showError("Failed to kick user: " + e.getMessage());
            }
        }
    }
    
    private void disconnect() {
        try {
            connected = false;
            
            if (output != null) {
                try {
                    Message logoutMsg = new Message(MessageType.LOGOUT, "ADMIN", "");
                    output.writeObject(logoutMsg);
                    output.flush();
                } catch (IOException e) {
                    // Ignore
                }
            }
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            SwingUtilities.invokeLater(() -> {
                updateStatus("⚪ Disconnected", false);
                connectButton.setText("Connect to Server");
                connectButton.setBackground(new Color(46, 204, 113));
                setControlsEnabled(false);
                
                clientListArea.setText("Connect to server to view connected clients...");
                chatHistoryArea.setText("Connect to server to view chat history...");
                systemStatsArea.setText("Connect to server to view statistics...");
            });
            
        } catch (IOException e) {
            showError("Error during disconnect: " + e.getMessage());
        }
    }
    
    private void setControlsEnabled(boolean enabled) {
        refreshClientsButton.setEnabled(enabled);
        refreshHistoryButton.setEnabled(enabled);
        refreshStatsButton.setEnabled(enabled);
        kickButton.setEnabled(enabled);
        kickUserField.setEnabled(enabled);
    }
    
    private void updateStatus(String status, boolean connected) {
        statusLabel.setText(status);
        if (connected) {
            statusLabel.setForeground(new Color(46, 204, 113));
        } else {
            statusLabel.setForeground(Color.WHITE);
        }
    }
    
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            AdminConsole console = new AdminConsole();
            console.setVisible(true);
        });
    }
}
