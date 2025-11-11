package client.ui;

import client.Client;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatClientUI extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Client client;

    public ChatClientUI(Client client) {
        this.client = client;
        setTitle("Chat - " + client.getUsername());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        sendButton.addActionListener(this::sendMessage);
        messageField.addActionListener(this::sendMessage);

        // Start listening for messages
        client.listenForMessages();

        setVisible(true);
    }

    private void sendMessage(ActionEvent e) {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            Message msg = new Message(MessageType.BROADCAST, client.getUsername(), text);
            client.sendMessage(msg);
            messageField.setText("");
        }
    }

    public void displayMessage(String msg) {
        chatArea.append(msg + "\n");
    }
}