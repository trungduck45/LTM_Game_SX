package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JoinRoomScreen extends JFrame {
    public JoinRoomScreen(String userId) {
        setTitle("Join Room");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a panel with a grid layout for the room cards
        JPanel roomPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add 10 buttons representing the rooms
        for (int i = 1; i <= 10; i++) {
            JButton roomButton = new JButton("Room " + i);
            String roomId = String.format("R%03d", i); // Capture the room ID for the action listener // Capture the room ID for the action listener
            roomButton.addActionListener(e -> joinRoom(userId, roomId));
            roomPanel.add(roomButton);
        }

        // Add the room panel to the frame
        add(roomPanel, BorderLayout.CENTER);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        // Set the frame to be visible
        setVisible(true);
    }

    private void joinRoom(String userId, String roomId) {
        try {
            Socket socket = new Socket("localhost", 12345); // Connect to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send join room request to the server
            out.println("JOIN_ROOM " + roomId + " " + userId);

            // Read the server's response
            String response = in.readLine();
            if ("JOIN_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Joined Room " + roomId + " successfully!");
                dispose();
                new GameScreen("localhost", userId);
                // Proceed to the game screen or other logic
            } else if ("ROOM_FULL".equals(response)) {
                JOptionPane.showMessageDialog(this, "Room " + roomId + " is full.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to join Room " + roomId);
            }

            // Close resources
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }
}