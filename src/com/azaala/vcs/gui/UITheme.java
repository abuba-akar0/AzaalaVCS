package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized UI Theme Management for Azaala VCS
 * Provides consistent colors, fonts, and styling across all GUI components
 */
public class UITheme {

    // ============= Color Palette =============
    // Primary Colors - Vibrant and Visible
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);        // Professional Blue
    public static final Color ACCENT_COLOR = new Color(39, 174, 96);          // Fresh Green
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252);    // Light Gray-Blue
    public static final Color PANEL_BACKGROUND = new Color(236, 241, 247);    // Slightly darker gray-blue

    // Text Colors - High Contrast
    public static final Color TEXT_PRIMARY = new Color(33, 47, 61);           // Very Dark Blue-Gray
    public static final Color TEXT_SECONDARY = new Color(89, 106, 122);       // Medium Blue-Gray
    public static final Color TEXT_ON_PRIMARY = new Color(68, 42, 144);     // White

    // Status Colors - More Vibrant
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);        // Vibrant Green
    public static final Color WARNING_COLOR = new Color(241, 196, 15);        // Vibrant Yellow
    public static final Color ERROR_COLOR = new Color(231, 76, 60);           // Vibrant Red

    // Borders & Separators - Better Visibility
    public static final Color BORDER_COLOR = new Color(189, 206, 221);        // Light Blue
    public static final Color SEPARATOR_COLOR = new Color(220, 230, 240);     // Very Light Blue

    // ============= Font Configuration =============
    private static final String[] FONT_FAMILIES = {
        "Segoe UI",      // Windows
        "Ubuntu",        // Linux
        "SF Pro Display", // macOS
        "Arial"          // Fallback
    };

    public static final Font TITLE_FONT = createFont(Font.BOLD, 18);
    public static final Font SUBTITLE_FONT = createFont(Font.BOLD, 15);
    public static final Font LABEL_FONT = createFont(Font.PLAIN, 13);
    public static final Font CONTENT_FONT = createFont(Font.PLAIN, 12);
    public static final Font MONOSPACE_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font SMALL_FONT = createFont(Font.PLAIN, 11);

    // ============= Padding & Spacing =============
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 12;
    public static final int PADDING_LARGE = 20;
    public static final int SPACING_COMPONENT = 8;
    public static final int SPACING_SECTION = 15;

    // ============= Component Dimensions =============
    public static final int BUTTON_HEIGHT = 40;
    public static final int INPUT_HEIGHT = 36;
    public static final int BORDER_RADIUS = 8;

    /**
     * Creates a font with the best available family
     */
    private static Font createFont(int style, int size) {
        for (String fontFamily : FONT_FAMILIES) {
            Font font = new Font(fontFamily, style, size);
            if (!font.getFamily().equals("Dialog")) { // Check if font is available
                return font;
            }
        }
        return new Font("Arial", style, size); // Fallback
    }

    /**
     * Applies the theme to a button
     */
    public static void styleButton(JButton button) {
        button.setFont(LABEL_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_ON_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(-1, BUTTON_HEIGHT));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        button.setOpaque(true);
    }

    /**
     * Applies the theme to a primary button (action buttons)
     */
    public static void stylePrimaryButton(JButton button) {
        styleButton(button);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_ON_PRIMARY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
    }

    /**
     * Applies the theme to a success button (commit, save, etc.)
     */
    public static void styleSuccessButton(JButton button) {
        styleButton(button);
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(TEXT_ON_PRIMARY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
    }

    /**
     * Applies the theme to a label
     */
    public static void styleLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_PRIMARY);
    }

    /**
     * Applies the theme to a title label
     */
    public static void styleTitleLabel(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_PRIMARY);
    }

    /**
     * Applies the theme to a text field
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(CONTENT_FONT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(PRIMARY_COLOR);
        textField.setPreferredSize(new Dimension(-1, INPUT_HEIGHT));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        textField.setBackground(BACKGROUND_COLOR);
        textField.setOpaque(true);
    }

    /**
     * Applies the theme to a text area
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(MONOSPACE_FONT);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setBackground(BACKGROUND_COLOR);
        textArea.setCaretColor(PRIMARY_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
    }

    /**
     * Applies the theme to a combo box
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(CONTENT_FONT);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(BACKGROUND_COLOR);
        comboBox.setPreferredSize(new Dimension(-1, INPUT_HEIGHT));
    }

    /**
     * Applies the theme to a table
     */
    public static void styleTable(JTable table) {
        table.setFont(CONTENT_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionForeground(TEXT_ON_PRIMARY);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setRowHeight(32);
        table.getTableHeader().setFont(SUBTITLE_FONT);
        table.getTableHeader().setBackground(PANEL_BACKGROUND);
        table.getTableHeader().setForeground(PRIMARY_COLOR);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
    }

    /**
     * Applies the theme to a list
     */
    public static void styleList(JList<?> list) {
        list.setFont(CONTENT_FONT);
        list.setForeground(TEXT_PRIMARY);
        list.setSelectionForeground(TEXT_ON_PRIMARY);
        list.setSelectionBackground(PRIMARY_COLOR);
        list.setFixedCellHeight(28);
    }

    /**
     * Creates an info panel with light background and title
     */
    public static JPanel createInfoPanel(String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(220, 237, 255)); // More vibrant light blue
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_LARGE, PADDING_MEDIUM, PADDING_LARGE)
        ));

        JLabel titleLabel = new JLabel("ℹ " + title);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(SPACING_COMPONENT));

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(CONTENT_FONT);
        descLabel.setForeground(TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);

        return panel;
    }

    /**
     * Creates a styled titled border
     */
    public static javax.swing.border.TitledBorder createStyledBorder(String title) {
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            title
        );
        border.setTitleColor(PRIMARY_COLOR);
        border.setTitleFont(SUBTITLE_FONT);
        return border;
    }

    /**
     * Creates a styled section panel
     */
    public static JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(createStyledBorder(title));
        return panel;
    }

    /**
     * Apply theme settings to the entire Look & Feel
     */
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Apply custom colors to common components
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", TEXT_ON_PRIMARY);
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextArea.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", PRIMARY_COLOR);
            UIManager.put("Table.selectionForeground", TEXT_ON_PRIMARY);
            UIManager.put("List.selectionBackground", PRIMARY_COLOR);
            UIManager.put("List.selectionForeground", TEXT_ON_PRIMARY);
        } catch (Exception e) {
            System.err.println("Error applying theme: " + e.getMessage());
        }
    }
}

