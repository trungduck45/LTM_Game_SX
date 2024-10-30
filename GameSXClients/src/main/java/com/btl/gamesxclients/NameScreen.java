package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NameScreen extends JFrame {
    private JTextField nameField;
    private JTextField ipField;  // Thêm trường nhập địa chỉ IP

    public NameScreen() {
        setTitle("Nhập tên người chơi");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel("Nhập tên của bạn:");
        nameField = new JTextField(20);

        JLabel ipLabel = new JLabel("Nhập địa chỉ IP của server:");
        ipField = new JTextField("localhost", 20);  // Đặt giá trị mặc định là 'localhost'

        JButton startButton = new JButton("Bắt đầu trò chơi");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5)); // GridLayout để bố trí gọn hơn
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(ipLabel);
        inputPanel.add(ipField);
        inputPanel.add(new JLabel());  // Đệm trống
        inputPanel.add(startButton);

        add(inputPanel, BorderLayout.CENTER);

        startButton.addActionListener(this::connectToServer);
        setVisible(true);
    }

    private void connectToServer(ActionEvent e) {
        String serverAddress = ipField.getText().trim();  // Lấy IP từ JTextField
        if (serverAddress.isEmpty()) return;

        new GameScreen(serverAddress, nameField.getText().trim());
        dispose();  // Đóng màn hình nhập tên
    }
}
