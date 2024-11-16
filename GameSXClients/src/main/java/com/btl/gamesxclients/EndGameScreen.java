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
    private String userId;
    private JLabel resultLabel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public EndGameScreen(String username, String roomId, String userId, int score) {
        this.userId = userId;
        setTitle("Kết thúc trò chơi");
        setSize(400, 300);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel roomLabel = new JLabel("Phòng chơi số " + roomId, SwingConstants.CENTER);
        JLabel userLabel = new JLabel("Bạn: " + username, SwingConstants.CENTER);
        JLabel scoreLabel = new JLabel("Số điểm: " + score, SwingConstants.CENTER);
        resultLabel = new JLabel("", SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(roomLabel, gbc);

        gbc.gridy = 1;
        add(userLabel, gbc);

        gbc.gridy = 2;
        add(scoreLabel, gbc);

        gbc.gridy = 3;
        add(resultLabel, gbc);

        JButton exitButton = new JButton("Thoát");
        exitButton.addActionListener(e -> exit());
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Set gridwidth to 2 to span both columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        add(exitButton, gbc);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    //    initializeServerConnection();

        sendScore(roomId, userId, String.valueOf(score));
       // startListeningForEvents();
        setVisible(true);
    }


    private void sendScore(String roomId, String userId, String score) {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("SEND_SCORE " + roomId + " " + userId + " " + score);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("RESULT")) {
                            System.out.println(response);
                            handleComparisonResult(response);
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error sending score to server.");
        }
    }
//    private void initializeServerConnection() {
//        try {
//            socket = new Socket("localhost", 12345);
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new Scanner(socket.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Error connecting to server.");
//        }
//    }
//
//    private void startListeningForEvents() {
//        new Thread(() -> {
//            while (true) {
//                if (in.hasNextLine()) {
//                    String response1 = in.nextLine();
//                    if (response1.startsWith("RESULT")) {
//                        String comparisonResult = response1;
//                        System.out.println(response1);
//                        handleComparisonResult(comparisonResult);
//                        break;
//                    }
//                }
//            }
//        }).start();
//    }

    private void handleComparisonResult(String comparisonResult) {
        String[] parts = comparisonResult.split(" ");
        if (userId.equals(parts[2])) {
            if (Integer.parseInt(parts[4]) > Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thắng!");
                resultLabel.setForeground(Color.GREEN);
            } else if (Integer.parseInt(parts[4]) < Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thua!");
                resultLabel.setForeground(Color.RED);
            } else {
                resultLabel.setText("Hòa!");
                resultLabel.setForeground(Color.ORANGE);
            }
        } else {
            if (Integer.parseInt(parts[4]) < Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thắng!");
                resultLabel.setForeground(Color.GREEN);
            } else if (Integer.parseInt(parts[4]) > Integer.parseInt(parts[5])) {
                resultLabel.setText("Bạn thua!");
                resultLabel.setForeground(Color.RED);
            } else {
                resultLabel.setText("Hòa!");
                resultLabel.setForeground(Color.ORANGE);
            }
        }
    }


    private void exit() {
        deleteRoom();
//        try {
//            if (socket != null) {
//                socket.close();
//            }
//            if (out != null) {
//                out.close();
//            }
//            if (in != null) {
//                in.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        dispose();
    }

    private void deleteRoom() {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("DELETE_ROOM " + userId);

            String response = in.readLine();
            if (!"DELETE_ROOM_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Failed to delete room.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }
}