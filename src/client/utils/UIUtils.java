package client.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Small UI utilities for consistent styling across the Swing UI.
 * This is a lightweight approach to improve appearance without moving to web-based UI.
 */
public class UIUtils {
	public static final Color PRIMARY = new Color(33, 150, 243);
	public static final Color PRIMARY_DARK = new Color(25, 118, 210);
	public static final Color BACKGROUND = new Color(250, 250, 250);
	public static final Color SURFACE = Color.WHITE;

	/**
	 * Apply a pleasant look-and-feel and set a default font.
	 */
	public static void initLookAndFeel() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception ignored) {}

		// set a sensible default font for the UI
		Font base = new Font("Segoe UI", Font.PLAIN, 14);
		UIManager.put("Label.font", base);
		UIManager.put("TextField.font", base);
		UIManager.put("TextArea.font", base);
		UIManager.put("Button.font", base.deriveFont(Font.BOLD));
		UIManager.put("List.font", base);
	}

	public static void styleButton(JButton btn) {
		btn.setBackground(PRIMARY);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
	}

	public static void styleTextField(JTextField tf) {
		tf.setBackground(SURFACE);
		tf.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220,220,220)),
				BorderFactory.createEmptyBorder(6,6,6,6)));
	}
}
