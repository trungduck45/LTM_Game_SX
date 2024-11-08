package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EndGameScreen extends JFrame {
    private String userId; // Add userId as an instance variable
    private JLabel resultLabel;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    public EndGameScreen(String username, String roomId, String userId, int score) {
        this.userId = userId;
        setTitle("Kết thúc trò chơi");
        setSize(400, 200);
        setLayout(new BorderLayout());

        // Hiển thị tên người chơi và điểm
        JLabel roomLabel = new JLabel("Phòng chơi số " + roomId, SwingConstants.CENTER);
        JLabel userLabel = new JLabel("Bạn: " + username, SwingConstants.CENTER);
        JLabel scoreLabel = new JLabel("Số điểm: " + score, SwingConstants.CENTER);

        // Initialize resultLabel
        resultLabel = new JLabel("", SwingConstants.CENTER);

        // Panel hiển thị
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(roomLabel);
        infoPanel.add(userLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(resultLabel); // Add resultLabel to the panel

        // Thêm nút Thoát
        JButton exitButton = new JButton("Thoát");
        exitButton.addActionListener(e -> exit());
        infoPanel.add(exitButton);

        // Căn giữa và thêm vào frame
        add(infoPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị ở giữa màn hình

        // Send score to the server
        sendScore(roomId, userId, String.valueOf(score));

        setVisible(true); // Show the frame
    }

    private void sendScore(String roomId, String userId, String score) {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            // Send score data to the server
            out.println("SEND_SCORE " + roomId + " " + userId + " " + score);

            new Thread(() -> {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String response = in.readLine();
                        if (response.startsWith("RESULT")) {
                            String comparisonResult = response; // Expecting SCORE_COMPARISON WIN/LOSE/DRAW
                            System.out.println(response);
                            handleComparisonResult(comparisonResult);
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                        break;
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error sending score to server.");
        }
    }

    private void handleComparisonResult(String comparisonResult) {
        // Now we process the comparison result from the server
        String[] parts = comparisonResult.split(" ");
        if (userId.equals(parts[2])) {
            if (Integer.parseInt(parts[4]) > Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thắng!");
                resultLabel.setForeground(Color.GREEN); // Change text color to green
            } else if (Integer.parseInt(parts[4]) < Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thua!");
                resultLabel.setForeground(Color.RED); // Change text color to red
            } else {
                resultLabel.setText("Hòa!");
                resultLabel.setForeground(Color.ORANGE); // Change text color to orange
            }
        } else {
            if (Integer.parseInt(parts[4]) < Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thắng!");
                resultLabel.setForeground(Color.GREEN); // Change text color to green
            } else if (Integer.parseInt(parts[4]) > Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thua!");
                resultLabel.setForeground(Color.RED); // Change text color to red
            } else {
                resultLabel.setText("Hòa!");
                resultLabel.setForeground(Color.ORANGE); // Change text color to orange
            }
        }
    }

    private void exit() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose(); // Close the frame
    }
}