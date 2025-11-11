package client.ui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        usernameField = new JTextField();
        loginButton = new JButton("Login");

        add(new JLabel("Enter username:"), BorderLayout.NORTH);
        add(usernameField, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);

        loginButton.addActionListener(this::handleLogin);

        setVisible(true);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!");
            return;
        }

        Client client = new Client(username);
        if (client.connect()) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            new ChatClientUI(client); // Open main chat window
            dispose(); // Close login window
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect to server.");
        }
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}