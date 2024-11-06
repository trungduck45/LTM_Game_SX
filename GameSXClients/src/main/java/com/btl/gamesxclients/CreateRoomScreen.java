package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CreateRoomScreen extends JFrame {
    private final String userId;
    private String roomId;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private  String player1;
    private  String player2;
    private  String message;

    public CreateRoomScreen(String userId, String roomId) {
        this.userId = userId;
        this.roomId = roomId;

        setTitle("Tạo phòng");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create UI components
        JLabel messageLabel = new JLabel("Waiting for another player...");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel roomIdLabel = new JLabel("Room ID: " + roomId);
        roomIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        roomIdLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton exitButton = new JButton("Thoát");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));

        // Set up the layout
        setLayout(new BorderLayout(10, 10));
        add(messageLabel, BorderLayout.NORTH);
        add(roomIdLabel, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);

        initializeServerConnection();

        // Add action listener to the button
        exitButton.addActionListener(e -> {
            deleteRoom();
            dispose();
        });

        // Initialize server connection and start listening for events
        startListeningForEvents();
    }

    private void initializeServerConnection() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }

    private void startListeningForEvents() {
        new Thread(() -> {
            while (true) {
                try {
                    String response = in.readLine();
                    if ("PLAYER_JOIN".equals(response)) {
                        out.println("GET_ROOM "+ roomId);
                        String response_room = in.readLine();
                        if (response_room != null && response_room.startsWith("ROOM_INFO:")) {
                            String rs = response_room.substring(10);
                            String[] rooms = rs.split(" ");
                            player1 = rooms[2];
                            player2 = rooms[3];
                            message = rooms[4];
                            System.out.println(player1+"/"+message);
                        } else {
                            System.out.println("Unexpected server response: " + response_room);
                            JOptionPane.showMessageDialog(this, "Failed to retrieve room information.");
                            break;
                        }
                        SwingUtilities.invokeLater(() -> {
                            new GameScreen("localhost", player1, roomId , player2, message).setVisible(true);
                            dispose();
                        });
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error reading from server.");
                    break;
                }
            }
        }).start();
    }

    private void deleteRoom() {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send delete room request to the server
            out.println("DELETE_ROOM " + userId);

            // Read server response
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
