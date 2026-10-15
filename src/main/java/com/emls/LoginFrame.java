package com.emls;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final AuthService auth = new AuthService();

    public LoginFrame() {
        super("Electronics Manufacturing Line Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 32));
        JLabel title = new JLabel("<html><div style='text-align:center'>ELECTRONICS MANUFACTURING<br>LINE MANAGEMENT SYSTEM</div></html>", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Username"), g);
        g.gridx = 1; form.add(username, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Password"), g);
        g.gridx = 1; form.add(password, g);
        root.add(form, BorderLayout.CENTER);

        JButton login = new JButton("Login");
        login.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(login, BorderLayout.CENTER);
        JLabel note = new JLabel("Authorized users only", SwingConstants.CENTER);
        note.setForeground(Color.GRAY);
        bottom.add(note, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void doLogin() {
        char[] pw = password.getPassword();
        try {
            Session session = auth.login(username.getText(), pw);
            if (session == null) {
                Db.info(this, "Invalid username/password or inactive account.");
                password.setText("");
                return;
            }
            dispose();
            new MainFrame(session).setVisible(true);
        } catch (Exception ex) {
            Db.error(this, ex);
        } finally {
            Arrays.fill(pw, '\0');
        }
    }
}
