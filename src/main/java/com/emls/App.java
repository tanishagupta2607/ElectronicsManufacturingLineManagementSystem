
package com.emls;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {}

            Color background = new Color(245, 247, 251);
            Color navy = new Color(23, 37, 84);
            Color blue = new Color(37, 99, 235);
            Color white = Color.WHITE;
            Color text = new Color(31, 41, 55);

            UIManager.put("Panel.background", background);
            UIManager.put("OptionPane.background", white);
            UIManager.put("TabbedPane.background", background);
            UIManager.put("TabbedPane.selected", white);
            UIManager.put("TabbedPane.font",
                    new Font("Segoe UI", Font.BOLD, 13));

            UIManager.put("Label.font",
                    new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Button.font",
                    new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("Button.background", blue);
            UIManager.put("Button.foreground", text);
            UIManager.put("Button.focus", background);

            UIManager.put("TextField.font",
                    new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("PasswordField.font",
                    new Font("Segoe UI", Font.PLAIN, 13));

            UIManager.put("Table.font",
                    new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.foreground", text);
            UIManager.put("Table.background", white);
            UIManager.put("Table.selectionBackground",
                    new Color(219, 234, 254));
            UIManager.put("Table.selectionForeground", navy);
            UIManager.put("Table.gridColor",
                    new Color(226, 232, 240));
            UIManager.put("Table.rowHeight", 30);

            UIManager.put("TableHeader.font",
                    new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("TableHeader.background",
                    new Color(226, 232, 240));
            UIManager.put("TableHeader.foreground", navy);

            UIManager.put("OptionPane.messageFont",
                    new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("OptionPane.buttonFont",
                    new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("ScrollPane.border",
                    BorderFactory.createEmptyBorder());

            new LoginFrame().setVisible(true);
        });
    }
}
