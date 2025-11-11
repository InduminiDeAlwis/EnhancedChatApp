package client.ui;

import client.Client;
import client.utils.UIUtils;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatClientUI.java
 * Main chat window for group chat
 * Displays broadcast messages and provides access to private chat
 */
public class ChatClientUI extends JFrame implements Client.MessageHandler {

    private Client client;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton privateChatButton;
    private JButton refreshUsersButton;
    private JButton disconnectButton;
    private JList<String> usersList;
    private DefaultListModel<String> usersListModel;

    // Store open private chat windows
    private Map<String, PrivateChatUI> privateChats;

    /**
     * Constructor
     */
    public ChatClientUI(Client client) {
        this.client = client;
        this.privateChats = new HashMap<>();

        initializeUI();

        // Start listening for messages from server
        client.startListening(this);

        // Request initial user list
        client.requestUserList();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        setTitle("Enhanced Chat - " + client.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create main panels
        createChatPanel();
        createUsersPanel();
        createInputPanel();

        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleDisconnect();
            }
        });

        UIUtils.centerWindow(this);
    }

    /**
     * Create chat display panel (center)
     */
    private void createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Title
        JLabel titleLabel = new JLabel("Group Chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);

        // Chat area
        chatArea = UIUtils.createTextArea(20, 50);
        JScrollPane scrollPane = UIUtils.createScrollPane(chatArea);

        chatPanel.add(titleLabel, BorderLayout.NORTH);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        add(chatPanel, BorderLayout.CENTER);
    }

    /**
     * Create users list panel (right side)
     */
    private void createUsersPanel() {
        JPanel usersPanel = new JPanel(new BorderLayout(5, 5));
        usersPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        usersPanel.setPreferredSize(new Dimension(200, 0));

        // Title
        JLabel usersLabel = new JLabel("Online Users");
        usersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usersLabel.setForeground(UIUtils.PRIMARY_COLOR);

        // Users list
        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        usersList.setFont(new Font("Arial", Font.PLAIN, 12));
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        privateChatButton = UIUtils.createButton("Private Chat", UIUtils.SECONDARY_COLOR);
        privateChatButton.addActionListener(e -> handlePrivateChat());

        refreshUsersButton = UIUtils.createButton("Refresh Users", UIUtils.PRIMARY_COLOR);
        refreshUsersButton.addActionListener(e -> client.requestUserList());

        disconnectButton = UIUtils.createButton("Disconnect", UIUtils.DANGER_COLOR);
        disconnectButton.addActionListener(e -> handleDisconnect());

        buttonsPanel.add(privateChatButton);
        buttonsPanel.add(refreshUsersButton);
        buttonsPanel.add(disconnectButton);

        // Double-click on user to open private chat
        usersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handlePrivateChat();
                }
            }
        });

        usersPanel.add(usersLabel, BorderLayout.NORTH);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);
        usersPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(usersPanel, BorderLayout.EAST);
    }

    /**
     * Create message input panel (bottom)
     */
    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Message input field
        messageField = UIUtils.createTextField("Type your message here...");
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.addActionListener(e -> handleSendMessage());

        // Send button
        sendButton = UIUtils.createButton("Send", UIUtils.PRIMARY_COLOR);
        sendButton.setPreferredSize(new Dimension(100, 35));
        sendButton.addActionListener(e -> handleSendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Handle send message button
     */
    private void handleSendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Check if it's a command
        if (message.equals("/users")) {
            client.requestUserList();
            messageField.setText("");
            return;
        }

        // Send broadcast message
        client.sendBroadcastMessage(message);

        // Clear input field
        messageField.setText("");
        messageField.requestFocus();
    }

    /**
     * Handle private chat button
     * Opens private chat window with selected user
     */
    private void handlePrivateChat() {
        String selectedUser = usersList.getSelectedValue();

        if (selectedUser == null) {
            UIUtils.showInfo(this,
                "Please select a user from the list to start a private chat.",
                "No User Selected");
            return;
        }

        // Don't allow private chat with yourself
        if (selectedUser.equals(client.getUsername())) {
            UIUtils.showInfo(this,
                "You cannot start a private chat with yourself!",
                "Invalid Selection");
            return;
        }

        // Check if private chat window already exists
        if (privateChats.containsKey(selectedUser)) {
            PrivateChatUI existingChat = privateChats.get(selectedUser);
            // Bring existing window to front
            existingChat.toFront();
            existingChat.requestFocus();
        } else {
            // Create new private chat window
            PrivateChatUI privateChatUI = new PrivateChatUI(client, selectedUser, this);
            privateChats.put(selectedUser, privateChatUI);
            privateChatUI.setVisible(true);
        }
    }

    /**
     * Remove private chat from map when window is closed
     */
    public void removePrivateChat(String username) {
        privateChats.remove(username);
    }

    /**
     * Handle disconnect button
     */
    private void handleDisconnect() {
        boolean confirm = UIUtils.showConfirmation(this,
            "Are you sure you want to disconnect?",
            "Confirm Disconnect");

        if (confirm) {
            // Close all private chat windows
            for (PrivateChatUI privateChatUI : privateChats.values()) {
                privateChatUI.dispose();
            }
            privateChats.clear();

            // Disconnect from server
            client.disconnect();

            // Close this window
            dispose();

            // Show message
            JOptionPane.showMessageDialog(null,
                "Disconnected from server. Goodbye!",
                "Disconnected",
                JOptionPane.INFORMATION_MESSAGE);

            System.exit(0);
        }
    }

    /**
     * Display message in chat area
     */
    private void displayMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message.toDisplayFormat() + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    /**
     * Update online users list
     */
    private void updateUsersList(String userListString) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.clear();

            // Parse user list: "Online users: Alice, Bob, Charlie"
            if (userListString.contains(":")) {
                String[] parts = userListString.split(":");
                if (parts.length > 1) {
                    String users = parts[1].trim();
                    if (!users.isEmpty()) {
                        String[] userArray = users.split(",");
                        for (String user : userArray) {
                            usersListModel.addElement(user.trim());
                        }
                    }
                }
            }
        });
    }

    /**
     * Handle received message from server
     * This is called by ClientListener when message arrives
     */
    @Override
    public void onMessageReceived(Message message) {
        switch (message.getType()) {
            case BROADCAST:
            case SYSTEM:
            case JOIN:
            case LEAVE:
                // Display in group chat
                displayMessage(message);
                break;

            case PRIVATE:
                // Route to appropriate private chat window
                handlePrivateMessage(message);
                break;

            case USER_LIST:
                // Update users list
                updateUsersList(message.getContent());
                break;

            case ERROR:
                // Show error message
                displayMessage(message);
                SwingUtilities.invokeLater(() -> {
                    UIUtils.showError(this, message.getContent(), "Error");
                });
                break;

            default:
                System.err.println("Unknown message type: " + message.getType());
                break;
        }
    }

    /**
     * Handle private message
     * Routes to appropriate private chat window or creates new one
     */
    private void handlePrivateMessage(Message message) {
        String sender = message.getSender();
        String myUsername = client.getUsername();

        // Determine who the "other person" is
        String otherPerson;
        if (sender.equals(myUsername)) {
            // Message sent by me, other person is recipient
            otherPerson = message.getRecipient();
        } else {
            // Message received from someone else
            otherPerson = sender;
        }

        // Check if private chat window exists
        if (privateChats.containsKey(otherPerson)) {
            // Window exists, route message to it
            PrivateChatUI privateChatUI = privateChats.get(otherPerson);
            privateChatUI.displayMessage(message);
        } else {
            // Window doesn't exist, create new one
            SwingUtilities.invokeLater(() -> {
                PrivateChatUI privateChatUI = new PrivateChatUI(client, otherPerson, this);
                privateChats.put(otherPerson, privateChatUI);
                privateChatUI.setVisible(true);
                privateChatUI.displayMessage(message);

                // Show notification
                privateChatUI.toFront();
                privateChatUI.requestFocus();
            });
        }
    }

    /**
     * Get current username
     */
    public String getUsername() {
        return client.getUsername();
    }
}
