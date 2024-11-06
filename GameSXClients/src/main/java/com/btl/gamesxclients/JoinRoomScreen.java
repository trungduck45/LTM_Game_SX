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
   // private  String roomId;
    private  String message;

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

        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            out.println("JOIN_ROOM " + roomId + " " + userId);
            String response = in.nextLine();
            if ("JOIN_SUCCESS".equals(response)) {
                //out.println("START_GAME " + roomId);
                messageLabel.setText("Joined room successfully.");

                String response_room = in.nextLine();
                if (response_room != null && response_room.startsWith("ROOM_INFO:")) {
                    String rs = response_room.substring(10);
                    String[] rooms = rs.split(" ");
                    player1 = rooms[2];
                    player2 = rooms[3];
                    message = rooms[4];
                    System.out.println(player2 +"/"+message);
                } else {
                    System.out.println("Unexpected server response: " + response_room);
                    JOptionPane.showMessageDialog(this, "Failed to retrieve room information.");

                }
                SwingUtilities.invokeLater(() -> {
                    new GameScreen("localhost", player2, roomId , player1,message).setVisible(true);
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