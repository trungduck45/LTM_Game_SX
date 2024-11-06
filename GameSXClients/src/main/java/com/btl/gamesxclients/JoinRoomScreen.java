package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class JoinRoomScreen extends JFrame {
    private final String userId;
    private String player1;
    private String player2;

    public JoinRoomScreen(String userId) {
        this.userId = userId;

        setTitle("Tìm phòng");
        setSize(400, 170);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create UI components
        JLabel roomIdLabel = new JLabel("Room ID:");
        roomIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField roomIdField = new JTextField(20);
        roomIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton joinButton = new JButton("Join");
        joinButton.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set up the layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Adjusted padding for top and bottom
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(roomIdLabel, gbc);

        gbc.gridx = 1;
        panel.add(roomIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(joinButton, gbc);

        gbc.gridy = 2;
        panel.add(messageLabel, gbc);

        add(panel);

        // Add action listener to the button
        joinButton.addActionListener(e -> {
            String roomId = roomIdField.getText();
            if (!roomId.isEmpty()) {
                joinRoom(roomId, messageLabel);
            } else {
                messageLabel.setText("Room ID cannot be empty.");
            }
        });
    }

    private void joinRoom(String roomId, JLabel messageLabel) {
        DatabaseConnection dbConnection = new DatabaseConnection();
        String users = dbConnection.getListUserDatabase(Integer.parseInt(roomId));
        System.out.println("Từ room :" + roomId + " Lấy ra từ DB các user: " + users);

        if (!users.isEmpty()) {
            // Split the IDs and handle cases where only one ID might be present
            String[] userIds = users.split(":");

            player1 = (userIds.length > 0) ? userIds[0] : "N/A";
            player2 = (userIds.length > 1) ? userIds[1] : "N/A";

            System.out.println("Player 1 ID: " + player1);
            System.out.println("Player 2 ID: " + player2);
        } else {
            System.out.println("No user IDs found for room ID: " + roomId);
        }


        //if( player1 == userId )
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send join room request to the server
            out.println("JOIN_ROOM " + roomId + " " + userId);

            // Read server response
            String response = in.nextLine();
            if ("JOIN_SUCCESS".equals(response)) {
                messageLabel.setText("Joined room successfully.");

                SwingUtilities.invokeLater(() -> {
                    new GameScreen("localhost", player1, roomId , player2).setVisible(true);
                    dispose();
                });
                SwingUtilities.invokeLater(() -> {
                    new GameScreen("localhost", player2, roomId , player1).setVisible(true);
                    dispose();
                });

            } else if ("ROOM_FULL".equals(response)) {
                messageLabel.setText("Room is full.");
            } else if ("ROOM_NOT_FOUND".equals(response)) {
                messageLabel.setText("Room not found.");
            } else {
                messageLabel.setText("Failed to join room.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error connecting to server.");
        }
    }
}