package client.ui;

import client.Client;
import client.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JButton loginButton;

    public LoginUI() {
        UIUtils.initLookAndFeel();

        setTitle("Login");
        setSize(360, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // root panel with padding
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(UIUtils.BACKGROUND);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel label = new JLabel("Enter username:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        UIUtils.styleTextField(usernameField);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(label);
        center.add(Box.createRigidArea(new Dimension(0,8)));
        center.add(usernameField);

        loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        UIUtils.styleButton(loginButton);

        root.add(center, BorderLayout.CENTER);
        root.add(loginButton, BorderLayout.SOUTH);

        loginButton.addActionListener(this::handleLogin);

        setContentPane(root);
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