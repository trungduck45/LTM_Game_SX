package com.btl.gamesxclients;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class RegisterScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField ingameNameField;
    private JButton registerButton;
    private JLabel successLabel;

    public RegisterScreen() {
        setTitle("Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        ingameNameField = new JTextField(15);
        registerButton = new JButton("Register");
        successLabel = new JLabel("");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Ingame Name:"), gbc);

        gbc.gridx = 1;
        panel.add(ingameNameField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(registerButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(successLabel, gbc);

        add(panel);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        setVisible(true);
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String ingameName = ingameNameField.getText();

        try {
            Socket socket = new Socket("localhost", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("REGISTER " + username + " " + password + " " + ingameName);

            // Assuming the server sends a response indicating success
            // For simplicity, we assume registration is always successful
            successLabel.setText("Thành công");
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
            ingameNameField.setEnabled(false);
            registerButton.setEnabled(false);

            out.close();
            socket.close();
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }
}