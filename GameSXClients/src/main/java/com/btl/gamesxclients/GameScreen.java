    package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameScreen extends JFrame {
    private List<JTextField> inputFields = new ArrayList<>();
    private List<JLabel> serverLabels = new ArrayList<>();
    private PrintWriter out;
    private BufferedReader in;

    public GameScreen(String serverAddress, String playerName) {
        try {
            Socket socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(playerName);  // Gửi tên người chơi đến server

            initGameUI();
            new Thread(new ServerListener(in, this)).start();  // Lắng nghe server

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void initGameUI() {
        setTitle("Trò chơi sắp xếp");
        setSize(500, 300);
        setLayout(new BorderLayout());

        JPanel serverRow = new JPanel();
        JPanel inputRow = new JPanel();

        // Các nhãn và ô nhập liệu sẽ được thêm vào sau khi nhận dữ liệu từ server
        add(serverRow, BorderLayout.NORTH);
        add(inputRow, BorderLayout.CENTER);

        JButton sendButton = new JButton("Gửi");
        sendButton.addActionListener(e -> sendSortedNumbers());

        add(sendButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void updateServerNumbers(String[] numbers) {
        resetInputFields();  // Reset các ô nhập trước khi cập nhật số mới

        // Xóa các nhãn và ô nhập cũ
        serverLabels.clear();
        inputFields.clear();

        JPanel serverRow = (JPanel) getContentPane().getComponent(0);
        JPanel inputRow = (JPanel) getContentPane().getComponent(1);

        for (String number : numbers) {
            // Tạo nhãn và ô nhập cho từng số
            JLabel serverLabel = new JLabel(number.trim());
            serverLabels.add(serverLabel);
            serverRow.add(serverLabel);

            JTextField inputField = new JTextField(2);
            inputFields.add(inputField);
            inputRow.add(inputField);
        }

        // Cập nhật giao diện sau khi thêm các thành phần mới
        serverRow.revalidate();
        serverRow.repaint();
        inputRow.revalidate();
        inputRow.repaint();
    }

    private void resetInputFields() {
        for (JTextField field : inputFields) {
            field.setText(""); // Đặt lại nội dung của ô nhập về chuỗi rỗng
        }
    }

    private void sendSortedNumbers() {
        try {
            StringBuilder sortedNumbers = new StringBuilder();
            for (JTextField field : inputFields) {
                int number = Integer.parseInt(field.getText().trim());
                sortedNumbers.append(number).append(",");
            }
            out.println(sortedNumbers.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
