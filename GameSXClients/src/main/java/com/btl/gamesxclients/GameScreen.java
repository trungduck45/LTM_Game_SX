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
    
    private JLabel scoreLabel; 

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
        scoreLabel = new JLabel("Điểm: 0");
        
        serverRow = new JPanel(new FlowLayout());
        inputRow = new JPanel(new FlowLayout());

        add(serverRow, BorderLayout.NORTH);
        add(inputRow, BorderLayout.CENTER);

        // Nút "Check"
        JButton sendButton = new JButton("Check");
        sendButton.addActionListener(e -> sendSortedNumbers());

        // Nút "Thoát Game"
        JButton exitButton = new JButton("Thoát Game");
        exitButton.addActionListener(e -> exitToNameScreen());

        // Tạo panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);

        add(scoreLabel, BorderLayout.WEST);  // Hiển thị điểm ở bên trái
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void updateServerNumbers(String[] numbers) {
        // Xóa các phần tử cũ trước khi thêm mới
        serverRow.removeAll();
        inputRow.removeAll();
        inputFields.clear(); // Xóa danh sách các ô nhập liệu cũ

        // Nối các số thành một chuỗi mà không có dấu ngoặc
        String numbersString = String.join(", ", numbers);

        // Tạo nhãn hiển thị chuỗi số
        JLabel serverLabel = new JLabel(numbersString);
        serverRow.add(serverLabel);

        // Thêm các ô nhập liệu tương ứng với mỗi số
        for (String number : numbers) {
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

    // Phương thức để quay về màn hình NameScreen
    private void exitToNameScreen() {
        dispose(); // Đóng cửa sổ GameScreen
        new NameScreen(); // Mở lại màn hình NameScreen
    }
    public void updateScore(String score) {
    SwingUtilities.invokeLater(() -> scoreLabel.setText("Điểm: " + score));
}

public void showResultMessage(String message) {
    SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(this, message);
    });
}
}
