package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;

public class WaitingRoomScreen extends JFrame {
    public WaitingRoomScreen(String username, String userId, String ingameName) {
        setTitle("Sảnh chờ");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Fetch user profile
        UserProfile userProfile = UserProfileService.getUserProfile(userId);

        // Create a panel for the title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Chào mừng " + userProfile.getIngameName() + " đến sảnh chờ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titlePanel.add(titleLabel);

        // Create a panel for the profile
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel ingameNameLabel = new JLabel("In-game Name: " + userProfile.getIngameName());
        ingameNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ingameNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel totalPointLabel = new JLabel("Total Point: " + userProfile.getTotalPoint());
        totalPointLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPointLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel rankedPointLabel = new JLabel("Ranked Point: " + userProfile.getRankedPoint());
        rankedPointLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        rankedPointLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        profilePanel.add(ingameNameLabel);
        profilePanel.add(totalPointLabel);
        profilePanel.add(rankedPointLabel);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        JButton enterGameButton = new JButton("Vào game");
        enterGameButton.addActionListener(e -> enterGame(userId));
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

    private void enterGame(String userId) {
        new JoinRoomScreen(userId); // Open RoomScreen
        dispose();  // Close the waiting room screen
    }
}