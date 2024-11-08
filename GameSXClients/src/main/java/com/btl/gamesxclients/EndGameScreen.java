package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class EndGameScreen extends JFrame {
    private String userId; // Add userId as an instance variable
    private JLabel resultLabel;

    public EndGameScreen(String username, String roomId,  String userId,int score) {
        this.userId = userId;
        setTitle("Kết thúc trò chơi");
        setSize(400, 200);
        setLayout(new BorderLayout());

        // Hiển thị tên người chơi và điểm
        JLabel roomLabel = new JLabel("Phòng chơi số " + roomId, SwingConstants.CENTER);
        JLabel userLabel = new JLabel("Bạn: " + username, SwingConstants.CENTER);
        JLabel scoreLabel = new JLabel("Số điểm: " + score, SwingConstants.CENTER);

        // Panel hiển thị
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(roomLabel);
        infoPanel.add(userLabel);
        infoPanel.add(scoreLabel);

        // Căn giữa và thêm vào frame
        add(infoPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị ở giữa màn hình

        // Send score to the server
        sendScore(roomId, userId, String.valueOf( score));

        // Timer to close the screen after 5 seconds
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                exit(); // Call exit after 5 seconds
//            }
//        }, 3000); // 3000 milliseconds = 3 seconds

        setVisible(true); // Show the frame
    }

    private void sendScore(String roomId, String userId, String score) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send score data to the server
            out.println("SEND_SCORE " + roomId + " " + userId + " " + score);

//            // Receive the server's response
//            int ok=1;
//            String response = in.nextLine();
//            if (response.startsWith("SCORE_SUCCESS: 1 player complete")) {
//                System.out.println("Score sent successfully.");
//                ok=0;
//                String response1 = in.nextLine();
//                if (response1.equals("SCORE_SUCCESS: 2 player complete")) {
//                    System.out.println("Score sent successfully.");
//
//                    String comparisonResult = in.nextLine(); // Expecting SCORE_COMPARISON WIN/LOSE/DRAW
//                    handleComparisonResult(comparisonResult);
//                }
//            } else if (response.equals("SCORE_SUCCESS: 2 player complete")) {
//                System.out.println("Score sent successfully.");
//
//                String comparisonResult = in.nextLine(); // Expecting SCORE_COMPARISON WIN/LOSE/DRAW
//                handleComparisonResult(comparisonResult);
//            } else {
//                System.err.println("Failed to send score. Server responded with: " + response);
//            }
            new Thread(() -> {
                String response = "";
                try {
                    response = in.nextLine();
                    if (response.startsWith("RESULT")) {
                        String comparisonResult = in.nextLine(); // Expecting SCORE_COMPARISON WIN/LOSE/DRAW
                        handleComparisonResult(comparisonResult);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
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

//        if (comparisonResult.contains("has a higher score")) {
//            // Extract the user who won the game from the comparison result
//            String winner = comparisonResult.split(" ")[1]; // Extract the winner's username
//            if (userId.equals(winner)) {
//                resultLabel.setText("Bạn thắng!");
//                resultLabel.setForeground(Color.GREEN); // Change text color to green
//            } else {
//                resultLabel.setText("Bạn thua!");
//                resultLabel.setForeground(Color.RED); // Change text color to red
//            }
//        } else if ("Both users have equal scores.".equals(comparisonResult)) {
//            resultLabel.setText("Hòa!");
//            resultLabel.setForeground(Color.ORANGE); // Change text color to orange
//        } else {
//            resultLabel.setText("Có lỗi xảy ra!");
//            resultLabel.setForeground(Color.BLACK); // Default color for errors
//        }
    }

    private void exit() {
        dispose(); // Close the frame
    }
}
