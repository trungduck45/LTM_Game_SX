package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class EndGameScreen extends JFrame {
    public EndGameScreen(String username, int score) {
        setTitle("Kết thúc trò chơi");
        setSize(500, 300);
        setLayout(new BorderLayout());

        // Hiển thị tên người chơi và điểm
        JLabel userLabel = new JLabel("Tên người chơi: " + username, SwingConstants.CENTER);
        JLabel scoreLabel = new JLabel("Số điểm: " + score, SwingConstants.CENTER);

        // Nút thoát game
        JButton exitButton = new JButton("Thoát Game");
        exitButton.addActionListener(e -> exit()); // Thoát ứng dụng

        // Panel hiển thị
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(userLabel);
        infoPanel.add(scoreLabel);

        // Căn giữa và thêm vào frame
        add(infoPanel, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị ở giữa màn hình

        // Timer to close the screen after 5 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                exit(); // Call exit after 5 seconds
            }
        }, 5000); // 5000 milliseconds = 5 seconds

        setVisible(true); // Show the frame
    }

    private void exit() {
        dispose(); // Close the frame
    }
}
