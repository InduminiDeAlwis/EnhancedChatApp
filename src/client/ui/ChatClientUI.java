package client.ui;

import client.Client;
import client.utils.UIUtils;
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
        UIUtils.initLookAndFeel();

        setTitle("Chat - " + client.getUsername());
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(UIUtils.BACKGROUND);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(UIUtils.SURFACE);
        chatArea.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        root.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);
        messageField = new JTextField();
        UIUtils.styleTextField(messageField);
        sendButton = new JButton("Send");
        UIUtils.styleButton(sendButton);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        root.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(this::sendMessage);
        messageField.addActionListener(this::sendMessage);

        // Start listening for messages
        client.listenForMessages();

        setContentPane(root);
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