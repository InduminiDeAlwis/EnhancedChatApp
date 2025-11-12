package client.ui;

import client.Client;
import client.utils.UIUtils;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PrivateChatUI.java
 * Private chat window for 1-to-1 conversations
 * This is YOUR FEATURE - the private messaging implementation!
 */
public class PrivateChatUI extends JFrame {

    private Client client;
    private String recipientUsername;
    private ChatClientUI parentWindow;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton closeButton;
    private JLabel statusLabel;

    /**
     * Constructor
     * @param client The client connection
     * @param recipientUsername The user we're chatting with
     * @param parentWindow Reference to main chat window
     */
    public PrivateChatUI(Client client, String recipientUsername, ChatClientUI parentWindow) {
        this.client = client;
        this.recipientUsername = recipientUsername;
        this.parentWindow = parentWindow;

        initializeUI();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        setTitle("Private Chat with " + recipientUsername);
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));

        // Set icon or indicator that this is private chat
        setIconImage(null); // You can set a custom icon here

        createHeaderPanel();
        createChatPanel();
        createInputPanel();

        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });

        // Position relative to parent window
        if (parentWindow != null) {
            Point parentLocation = parentWindow.getLocation();
            setLocation(parentLocation.x + 50, parentLocation.y + 50);
        } else {
            UIUtils.centerWindow(this);
        }
    }

    /**
     * Create header panel with status and close button
     */
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.setBackground(UIUtils.PRIVATE_MSG_COLOR);

        // Title label
        JLabel titleLabel = new JLabel("Private Conversation with " + recipientUsername);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        // Info label
        JLabel infoLabel = new JLabel("Only you and " + recipientUsername + " can see these messages");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(240, 240, 240));

        // Title panel (vertical layout)
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(infoLabel);

        // Close button
        closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 24));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(UIUtils.PRIVATE_MSG_COLOR);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText("Close this private chat");
        closeButton.addActionListener(e -> handleClose());

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Create chat display panel
     */
    private void createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Chat area
        chatArea = UIUtils.createTextArea(20, 50);
        chatArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = UIUtils.createScrollPane(chatArea);

        // Welcome message
        chatArea.append("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP\n");
        chatArea.append("  Private Chat Started\n");
        chatArea.append("  You can now send private messages to " + recipientUsername + "\n");
        chatArea.append("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP\n\n");

        chatPanel.add(scrollPane, BorderLayout.CENTER);

        add(chatPanel, BorderLayout.CENTER);
    }

    /**
     * Create message input panel
     */
    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Message input field
        messageField = UIUtils.createTextField("Type your private message...");
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.addActionListener(e -> handleSendMessage());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        sendButton = UIUtils.createButton("Send Private Message", UIUtils.PRIVATE_MSG_COLOR);
        sendButton.setPreferredSize(new Dimension(180, 35));
        sendButton.addActionListener(e -> handleSendMessage());

        buttonPanel.add(sendButton);

        // Status label (for typing indicators, etc.)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        statusLabel.setForeground(UIUtils.SYSTEM_MSG_COLOR);

        // Input container
        JPanel inputContainer = new JPanel(new BorderLayout(5, 5));
        inputContainer.add(messageField, BorderLayout.CENTER);
        inputContainer.add(buttonPanel, BorderLayout.EAST);

        inputPanel.add(inputContainer, BorderLayout.CENTER);
        inputPanel.add(statusLabel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Handle send message button
     * This is where the magic happens - sending private messages!
     */
    private void handleSendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Send private message using Client.sendPrivateMessage()
        // This will go to Server -> ClientHandler -> recipient's Client
        client.sendPrivateMessage(recipientUsername, message);

        // Note: We don't display here because the message will come back
        // from the server and be displayed via displayMessage()

        // Clear input field
        messageField.setText("");
        messageField.requestFocus();

        // Show temporary status
        showStatus("Sending...");

        // Clear status after 1 second
        Timer timer = new Timer(1000, e -> showStatus(" "));
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Display message in chat area
     * Called by ChatClientUI when private message is received
     */
    public void displayMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            // Format the message for display
            String sender = message.getSender();
            String displayName;

            if (sender.equals(client.getUsername())) {
                displayName = "You";
            } else {
                displayName = sender;
            }

            // Append to chat area with special formatting for private messages
            chatArea.append(message.toDisplayFormat() + "\n");

            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());

            // If window is minimized, bring to attention
            if (getState() == Frame.ICONIFIED) {
                setState(Frame.NORMAL);
            }

            // Flash window if not focused (platform dependent)
            if (!isFocused()) {
                // You could add a notification sound here
                toFront();
            }
        });
    }

    /**
     * Show status message
     */
    private void showStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }

    /**
     * Handle close button
     */
    private void handleClose() {
        int result = JOptionPane.showConfirmDialog(this,
            "Close private chat with " + recipientUsername + "?",
            "Close Private Chat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Remove from parent's map
            if (parentWindow != null) {
                parentWindow.removePrivateChat(recipientUsername);
            }

            // Close window
            dispose();
        }
    }

    /**
     * Get recipient username
     */
    public String getRecipientUsername() {
        return recipientUsername;
    }

    /**
     * Bring window to front (called when user tries to open duplicate window)
     */
    public void bringToFront() {
        setState(Frame.NORMAL);
        toFront();
        requestFocus();
        messageField.requestFocus();
    }
}
