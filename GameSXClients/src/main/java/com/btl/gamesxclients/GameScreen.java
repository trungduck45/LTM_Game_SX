package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameScreen extends JFrame {
    private List<JTextField> inputFields = new ArrayList<>();
    private JPanel serverRow;
    private JPanel inputRow;
    private PrintWriter out;
    private BufferedReader in;

    public GameScreen(String serverAddress, String playerName) {
        try {
            Socket socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(playerName); // Gửi tên người chơi tới server

            initGameUI();
            new Thread(new ServerListener(in, this)).start(); // Lắng nghe dữ liệu từ server

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void initGameUI() {
        setTitle("Trò chơi sắp xếp");
        setSize(500, 300);
        setLayout(new BorderLayout());

        serverRow = new JPanel(new FlowLayout());
        inputRow = new JPanel(new FlowLayout());

        add(serverRow, BorderLayout.NORTH);
        add(inputRow, BorderLayout.CENTER);

        JButton sendButton = new JButton("Check");
        sendButton.addActionListener(e -> sendSortedNumbers());

        add(sendButton, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void updateServerNumbers(String[] numbers) {
        // Xóa các phần tử cũ trước khi thêm mới
        serverRow.removeAll();
        inputRow.removeAll();
        inputFields.clear(); // Xóa danh sách các ô nhập liệu cũ

        for (String number : numbers) {
            // Tạo nhãn cho mỗi số nhận từ server
            JLabel serverLabel = new JLabel(number.trim());
            serverRow.add(serverLabel);

            // Tạo ô nhập liệu để người dùng nhập số đã sắp xếp
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

    private void sendSortedNumbers() {
        try {
            StringBuilder sortedNumbers = new StringBuilder();
            for (JTextField field : inputFields) {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                sortedNumbers.append(Integer.parseInt(text)).append(",");
            }
            // Xóa dấu phẩy cuối cùng
            if (sortedNumbers.length() > 0) {
                sortedNumbers.setLength(sortedNumbers.length() - 1);
            }
            out.println(sortedNumbers.toString()); // Gửi dãy số đã sắp xếp tới server
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
