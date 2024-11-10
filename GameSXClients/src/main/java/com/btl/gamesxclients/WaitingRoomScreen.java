package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WaitingRoomScreen extends JFrame {
    private String serverAddress = "localhost"; // Ensure this is correct
    private int serverPort = 12345; // Ensure this is correct

    public WaitingRoomScreen(String username, String userId, String ingameName) {
        setTitle("Sảnh chờ");
        setSize(600, 400);
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

//        JLabel rankedPointLabel = new JLabel("Ranked Point: " + userProfile.getRankedPoint());
//        rankedPointLabel.setFont(new Font("Arial", Font.PLAIN, 14));
//        rankedPointLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        profilePanel.add(ingameNameLabel);
        profilePanel.add(totalPointLabel);
//        profilePanel.add(rankedPointLabel);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        JButton enterGameButton = new JButton("Tìm phòng");
        enterGameButton.addActionListener(e -> joinRoom(userId));
        buttonPanel.add(enterGameButton);

        JButton createRoomButton = new JButton("Tạo phòng");
        createRoomButton.addActionListener(e -> createRoom(userId));
        buttonPanel.add(createRoomButton);
        
        JButton playerlistButton = new JButton("Danh sách người chơi");
        playerlistButton.addActionListener(e -> playerList());
        buttonPanel.add(playerlistButton);

        // Create a logout button
        JButton logoutButton = new JButton("Đăng Xuất");
        logoutButton.addActionListener(e -> logout(username));
        buttonPanel.add(logoutButton);

        // Add panels to the frame
        add(titlePanel, BorderLayout.NORTH);
        add(profilePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        // Set the frame to be visible
        setVisible(true);
    }
    private void joinRoom(String userId) {
        new JoinRoomScreen(userId).setVisible(true); // Open RoomScreen
    }
    private void playerList(){
        new PlayerListScreen();
    }

    private void createRoom(String userId) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send create room request to the server
            out.println("CREATE_ROOM " + userId);

            // Read server response
            String response = in.nextLine();
            if (response.startsWith("CREATE_ROOM_SUCCESS")) {
                if (response.split(" ").length == 2) {
                    String roomId = response.split(" ")[1];
                    new CreateRoomScreen(userId, roomId).setVisible(true); // Open CreateRoomScreen
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create room.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create room.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }

    private void logout(String username) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send logout request to the server
            out.println("LOGOUT " + username);

            // Read server response
            String response = in.nextLine();
            if ("LOGOUT_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Đăng xuất thành công!");
                dispose(); // Close the WaitingRoomScreen
                new LoginScreen(); // Open login screen
            } else {
                JOptionPane.showMessageDialog(this, "Đăng xuất thất bại!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối đến server.");
        }
    }

}