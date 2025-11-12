package client.utils;

import javax.swing.*;
import java.awt.*;

/**
 * UIUtils.java
 * Utility class for common UI operations
 * Provides reusable UI components and styling
 */
public class UIUtils {

    // Color scheme for the application - More vibrant and visible!
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);      // Bright Blue
    public static final Color SECONDARY_COLOR = new Color(76, 175, 80);     // Green
    public static final Color DANGER_COLOR = new Color(244, 67, 54);        // Red
    public static final Color BACKGROUND_COLOR = new Color(250, 250, 250);  // Almost white
    public static final Color TEXT_COLOR = new Color(33, 33, 33);           // Almost black
    public static final Color SYSTEM_MSG_COLOR = new Color(158, 158, 158);  // Gray
    public static final Color PRIVATE_MSG_COLOR = new Color(156, 39, 176);  // Purple

    /**
     * Create a styled JButton
     */
    public static JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setOpaque(true); // Make sure background color shows
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    /**
     * Create a styled JTextField
     */
    public static JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    /**
     * Create a styled JTextArea
     */
    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea textArea = new JTextArea(rows, cols);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        return textArea;
    }

    /**
     * Create a styled JScrollPane
     */
    public static JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        return scrollPane;
    }

    /**
     * Create a titled panel
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            title,
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TEXT_COLOR
        ));
        return panel;
    }

    /**
     * Show error dialog
     */
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show information dialog
     */
    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Show input dialog
     */
    public static String showInputDialog(Component parent, String message, String title) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Center window on screen
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    /**
     * Set window icon (if available)
     */
    public static void setWindowIcon(JFrame frame, String iconPath) {
        try {
            ImageIcon icon = new ImageIcon(iconPath);
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, use default
            System.err.println("Could not load icon: " + iconPath);
        }
    }

    /**
     * Create a horizontal spacer
     */
    public static Component createHorizontalSpacer(int width) {
        return Box.createRigidArea(new Dimension(width, 0));
    }

    /**
     * Create a vertical spacer
     */
    public static Component createVerticalSpacer(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    /**
     * Append colored text to JTextArea
     * Note: JTextArea doesn't support colors directly,
     * For colored text, consider using JTextPane or HTML
     */
    public static void appendColoredText(JTextArea textArea, String text, Color color) {
        // For simple JTextArea, just append text
        // For colored text, you would need to use JTextPane instead
        textArea.append(text + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    /**
     * Get color for message type
     */
    public static Color getMessageColor(String messageType) {
        switch (messageType.toUpperCase()) {
            case "SYSTEM":
                return SYSTEM_MSG_COLOR;
            case "PRIVATE":
                return PRIVATE_MSG_COLOR;
            case "ERROR":
                return DANGER_COLOR;
            default:
                return TEXT_COLOR;
        }
    }
}
