package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NameScreen extends JFrame {
    private JTextField nameField;

    public NameScreen() {
        setTitle("Nhập tên người chơi");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel("Nhập tên của bạn:");
        nameField = new JTextField(20);
        JButton startButton = new JButton("Bắt đầu trò chơi");

        JPanel inputPanel = new JPanel();
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(startButton);

        add(inputPanel, BorderLayout.CENTER);

        startButton.addActionListener(this::connectToServer);
        setVisible(true);
    }

    private void connectToServer(ActionEvent e) {
        String serverAddress = JOptionPane.showInputDialog(this, "Nhập địa chỉ IP của server:", "localhost");
        if (serverAddress == null || serverAddress.trim().isEmpty()) return;

        new GameScreen(serverAddress, nameField.getText().trim());
        dispose();  // Đóng màn hình nhập tên
    }
}
