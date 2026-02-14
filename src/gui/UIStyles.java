
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UI Styles - Centralized styling for consistent look and feel
 */
public class UIStyles {

    // Color Palette - Modern Dark Theme
    public static final Color PRIMARY = new Color(79, 70, 229);      // Indigo
    public static final Color PRIMARY_DARK = new Color(67, 56, 202);  // Darker Indigo
    public static final Color SUCCESS = new Color(34, 197, 94);       // Green
    public static final Color DANGER = new Color(239, 68, 68);        // Red
    public static final Color WARNING = new Color(245, 158, 11);      // Amber
    public static final Color INFO = new Color(59, 130, 246);         // Blue

    public static final Color BG_DARK = new Color(17, 24, 39);        // Dark background
    public static final Color BG_CARD = new Color(31, 41, 55);        // Card background
    public static final Color BG_INPUT = new Color(55, 65, 81);       // Input background

    public static final Color TEXT_PRIMARY = new Color(249, 250, 251); // White text
    public static final Color TEXT_SECONDARY = new Color(156, 163, 175); // Gray text
    public static final Color TEXT_MUTED = new Color(107, 114, 128);   // Muted text

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // Borders
    public static final Border BORDER_EMPTY = BorderFactory.createEmptyBorder(20, 20, 20, 20);
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(75, 85, 99), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );

    /**
     * Style a primary button
     */
    public static void styleButton(JButton button, Color bgColor) {
        button.setFont(FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 40));

        // Add hover effect
        Color hoverColor = bgColor.darker();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    /**
     * Style a text field
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_BODY);
        textField.setBackground(BG_INPUT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 85, 99), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 45));
    }

    /**
     * Style a password field
     */
    public static void stylePasswordField(JPasswordField passwordField) {
        passwordField.setFont(FONT_BODY);
        passwordField.setBackground(BG_INPUT);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setCaretColor(TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 85, 99), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 45));
    }

    /**
     * Style a label
     */
    public static void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
    }

    /**
     * Create a styled panel
     */
    public static JPanel createStyledPanel(LayoutManager layout, Color bgColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(bgColor);
        return panel;
    }

    /**
     * Create a card panel with border
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BORDER_CARD);
        return panel;
    }

    /**
     * Style a table
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(75, 85, 99));
        table.setRowHeight(35);
        table.getTableHeader().setFont(FONT_BUTTON);
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
    }

    /**
     * Get status color based on status string
     */
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