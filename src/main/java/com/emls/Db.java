package com.emls;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Db {
    private Db() {}

    public static DefaultTableModel model() {
        return new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    public static void loadTable(JTable table, String sql, Object... params) throws SQLException {
        DefaultTableModel model = model();
        try (Connection c = Config.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) model.addColumn(md.getColumnLabel(i));
                while (rs.next()) {
                    Object[] row = new Object[md.getColumnCount()];
                    for (int i = 1; i <= row.length; i++) row[i - 1] = rs.getObject(i);
                    model.addRow(row);
                }
            }
        }
        table.setModel(model);
    }

    public static long scalarLong(String sql) throws SQLException {
        try (Connection c = Config.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    public static String scalarString(String sql) throws SQLException {
        try (Connection c = Config.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : "";
        }
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "EMLS", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, Exception ex) {
        String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Operation failed", JOptionPane.ERROR_MESSAGE);
    }

    public static String input(Component parent, String prompt) {
        return JOptionPane.showInputDialog(parent, prompt);
    }
    public static Long selectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object val = table.getValueAt(row, 0);
        return val == null ? null : Long.valueOf(val.toString());
    }
}
