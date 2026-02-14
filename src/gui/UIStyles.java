package gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UIStyles - Centralized styling for SIDRS GUI
 * Enhanced dark theme with better contrast and readability
 */
public class UIStyles {

    // -------------------- Colors --------------------
    public static final Color PRIMARY = new Color(99, 102, 241);      // Brighter Indigo
    public static final Color PRIMARY_DARK = new Color(79, 70, 229);  // Indigo
    public static final Color SUCCESS = new Color(34, 214, 124);      // Vibrant Green
    public static final Color DANGER = new Color(255, 85, 85);        // Brighter Red
    public static final Color WARNING = new Color(255, 189, 89);      // Softer Amber
    public static final Color INFO = new Color(96, 165, 250);         // Brighter Blue

    public static final Color BG_DARK = new Color(24, 28, 43);        // Dark background
    public static final Color BG_CARD = new Color(39, 44, 59);        // Card background
    public static final Color BG_INPUT = new Color(54, 61, 79);       // Input fields

    public static final Color TEXT_PRIMARY = new Color(242, 242, 247); // Bright white text
    public static final Color TEXT_SECONDARY = new Color(180, 185, 200); // Soft gray
    public static final Color TEXT_MUTED = new Color(130, 135, 150);    // Muted gray

    // -------------------- Fonts --------------------
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);

    // -------------------- Borders --------------------
    public static final Border BORDER_EMPTY = BorderFactory.createEmptyBorder(20, 20, 20, 20);
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 115, 140), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );

    // -------------------- Buttons --------------------
    public static void styleButton(JButton button, Color bgColor) {
        button.setFont(FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 40));

        // Hover effect (slightly brighter)
        Color hoverColor = bgColor.brighter();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    // -------------------- Text Fields --------------------
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_BODY);
        textField.setBackground(BG_INPUT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 115, 140), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 45));
    }

    public static void stylePasswordField(JPasswordField passwordField) {
        passwordField.setFont(FONT_BODY);
        passwordField.setBackground(BG_INPUT);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setCaretColor(TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 115, 140), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 45));
    }

    // -------------------- Labels --------------------
    public static void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
    }

    // -------------------- Panels --------------------
    public static JPanel createStyledPanel(LayoutManager layout, Color bgColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(bgColor);
        return panel;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BORDER_CARD);
        return panel;
    }

    // -------------------- Tables --------------------
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(100, 115, 140));
        table.setRowHeight(35);
        table.getTableHeader().setFont(FONT_BUTTON);
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
    }


    // -------------------- Status Colors --------------------
    public static Color getStatusColor(String status) {
        if (status == null) return TEXT_MUTED;

        switch (status.toUpperCase()) {
            case "SUCCESS":
            case "ACTIVE":
                return SUCCESS;
            case "FAILED":
            case "BLOCKED":
            case "LOCKED":
            case "SUSPENDED":
                return DANGER;
            case "ADMIN":
                return PRIMARY;
            default:
                return WARNING;
        }
    }
}
