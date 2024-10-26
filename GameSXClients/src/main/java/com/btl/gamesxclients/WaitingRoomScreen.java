package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;

public class WaitingRoomScreen extends JFrame {
    public WaitingRoomScreen(String playerName) {
        setTitle("Sảnh chờ");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a panel for the title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Chào mừng " + playerName + " đến sảnh chờ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titlePanel.add(titleLabel);

        // Create a panel for the profile
        JPanel profilePanel = new JPanel();
        JLabel profileLabel = new JLabel("Profile: " + playerName);
        profileLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        profilePanel.add(profileLabel);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        JButton enterGameButton = new JButton("Vào game");
        enterGameButton.addActionListener(e -> enterGame(playerName));
        buttonPanel.add(enterGameButton);

        // Add panels to the frame
        add(titlePanel, BorderLayout.NORTH);
        add(profilePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        // Set the frame to be visible
        setVisible(true);
    }

    private void enterGame(String playerName) {
        new GameScreen("localhost", playerName); // Open GameScreen
        dispose();  // Close the waiting room screen
    }
}