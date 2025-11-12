package client.ui;

import client.Client;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PrivateChatUI extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Client client;
    private String targetUser;

    public PrivateChatUI(Client client, String targetUser) {
        this.client = client;
        this.targetUser = targetUser;

        setTitle("Private Chat with " + targetUser);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(this::sendPrivateMessage);
        messageField.addActionListener(this::sendPrivateMessage);

        setVisible(true);
    }

    private void sendPrivateMessage(ActionEvent e) {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            Message msg = new Message(MessageType.PRIVATE, client.getUsername(), text);
            msg.setTargetUser(targetUser);
            client.sendMessage(msg);
            messageField.setText("");
        }
    }

    public void displayMessage(String msg) {
        chatArea.append(msg + "\n");
    }
}