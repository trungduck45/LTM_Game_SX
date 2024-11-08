package com.btl.gamesxclients;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class WaitingRoomScreen extends JFrame {
    private String serverAddress = "localhost"; // Ensure this is correct
    private int serverPort = 12345; // Ensure this is correct
    private DefaultTableModel onlineTableModel;
    private JTable onlinePlayersTable;
    private JButton challengeButton; // Button for challenging an online user
    private String selectedPlayer; // Store the selected player
    private String selectedPlayerId; // Lưu ID của người chơi được chọn
    private String userId;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private String roomId;
    private  String doithuId;
    public WaitingRoomScreen(String username, String userId, String ingameName) {

        this.userId = userId;
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

        profilePanel.add(ingameNameLabel);
        profilePanel.add(totalPointLabel);

        // Create a panel for online players
        JPanel onlinePlayersPanel = new JPanel();
        onlinePlayersPanel.setLayout(new BorderLayout());
        onlinePlayersPanel.setBorder(BorderFactory.createTitledBorder("Người chơi trực tuyến"));

        // Create a table to display online players
        String[] columnNames = {"User ID", "User Name", "Total Point", "Status"};
        onlineTableModel = new DefaultTableModel(columnNames, 0);
        onlinePlayersTable = new JTable(onlineTableModel);
        JScrollPane onlineScrollPane = new JScrollPane(onlinePlayersTable);
        onlinePlayersPanel.add(onlineScrollPane, BorderLayout.CENTER);

        // Add mouse listener to handle row selection
        onlinePlayersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = onlinePlayersTable.getSelectedRow();
                if (row != -1) {
                    selectedPlayerId = (String) onlineTableModel.getValueAt(row, 0); // Lấy tên người chơi
                    selectedPlayer = (String) onlineTableModel.getValueAt(row, 1); // Lấy ID người chơi (giả sử bạn đã thay đổi cột tương ứng trong bảng)
                    challengeButton.setEnabled(true); // Kích hoạt nút thách đấu
                }
            }
        });

        // Fetch online players initially
        updateOnlinePlayers();

        // Create a Timer to refresh the online players list every 5 seconds
//        Timer timer = new Timer(5000, e -> updateOnlinePlayers());
//        timer.start(); // Start the timer

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

        // Create the challenge button, initially disabled
        challengeButton = new JButton("Thách Đấu");
        challengeButton.setEnabled(false);
        challengeButton.addActionListener(e -> challengePlayer(userId));
        buttonPanel.add(challengeButton);

        // Create a logout button
        JButton logoutButton = new JButton("Đăng Xuất");
        logoutButton.addActionListener(e -> logout(username));
        buttonPanel.add(logoutButton);

        // Add panels to the frame
        add(titlePanel, BorderLayout.NORTH);
        add(profilePanel, BorderLayout.WEST);
        add(onlinePlayersPanel, BorderLayout.CENTER); // Add the online players panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        // Set the frame to be visible
        setVisible(true);
        initializeServerConnection();
        startListeningForEvents2();
//        startListeningForChallege();
    }

    private void updateOnlinePlayers() {
        // Clear the current model
        onlineTableModel.setRowCount(0);

        // Fetch online players
        List<UserProfile> onlinePlayers = UserProfileService.getOnlinePlayers();
        for (UserProfile player : onlinePlayers) {
            Object[] rowData = {player.getUserId(), player.getIngameName(), player.getTotalPoint(), player.getStatus()};
            onlineTableModel.addRow(rowData);
        }
    }

    private void challengePlayer(String userId) {
        // Hiển thị hộp thoại xác nhận thách đấu
        int response1 = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn thách đấu người chơi " + selectedPlayer + " không?",
                "Thách Đấu",
                JOptionPane.YES_NO_CANCEL_OPTION);

        // Kiểm tra phản hồi từ hộp thoại
        if (response1 == JOptionPane.YES_OPTION) {
            // Khởi tạo socket để gửi yêu cầu


            // Khởi tạo và bắt đầu luồng lắng nghe
//                ServerListener serverListener = new ServerListener(socket, this);
//                Thread listenerThread = new Thread(serverListener);
//                listenerThread.start(); // Bắt đầu luồng lắng nghe

            // Gửi yêu cầu thách đấu tới backend
            createRoomForChallenge(userId);
            out.println("CHALLENGE " + selectedPlayerId + " " + userId + " " + roomId);
            SwingUtilities.invokeLater(() -> {
                new WaitingChallengeScreen(userId, roomId).setVisible(true);
                dispose();
            });
        }else if (response1 == JOptionPane.NO_OPTION) {
            // Gọi hàm deleteRoom nếu người dùng chọn No
            JOptionPane.showMessageDialog(this, "Thách đấu đã bị hủy.");
        }
    }

    private void initializeServerConnection() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }
    private void startListeningForEvents2() {
        new Thread(() -> {
            while (true) {
                if (in.hasNextLine()) {
                    String response = in.nextLine();
                    if (response.startsWith("CHALLENGE_TO:" + userId)) {
                        String[] parts = response.split(" ");
                        this.doithuId = parts[1];
                        this.roomId = parts[2];
                        System.out.println("message là: " + response);

                        // Hiển thị thông báo với lựa chọn Yes và No
                        int result = JOptionPane.showConfirmDialog(null,
                                "Người chơi " + doithuId + " đã gửi lời thách đấu. Bạn có đồng ý không?",
                                "Thách đấu",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        // Xử lý lựa chọn của người dùng
                        if (result == JOptionPane.YES_OPTION) {
                            System.out.println("Bạn đã đồng ý thách đấu!");
                            joinRoomForChallenge(this.roomId, userId);
                        } else if (result == JOptionPane.NO_OPTION) {
                            System.out.println("Bạn đã từ chối thách đấu!");
                            out.println("khong choi ");
                        }
                    }


                }
            }
        }).start();
    }

    private void startListeningForChallege() {
        new Thread(() -> {
            while (true) {
                if (in.hasNextLine()) {
                    String response = in.nextLine();
                    if ("PLAYER_JOIN".equals(response)) {
                        SwingUtilities.invokeLater(() -> {
                            new GameScreen("localhost", userId).setVisible(true);
                            dispose();
                        });
                        break;
                    }
                }
            }
        }).start();
    }


    private void joinRoom(String userId ) {
        new JoinRoomScreen(userId).setVisible(true); // Open RoomScreen
    }

    private void joinRoomForChallenge(String roomId, String userId) {

            // Send join room request to the server
            out.println("JOIN_ROOM " + roomId + " " + userId);

            // Read server response
            String response = in.nextLine();
            if ("JOIN_SUCCESS".equals(response)) {
                System.out.println("Join Success");
                SwingUtilities.invokeLater(() -> {
                    new GameScreen("localhost", userId).setVisible(true);
                    dispose();
                });
            } else if ("ROOM_FULL".equals(response)) {
                System.out.println("Room is full");
            } else if ("ROOM_NOT_FOUND".equals(response)) {
                System.out.println("Room is not found");
            } else {
                System.out.println("Failed");
            }

    }

    private void playerList() {
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

    private void createRoomForChallenge(String userId) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send create room request to the server
            out.println("CREATE_ROOM " + userId);

            // Read server response
            String response = in.nextLine();
            System.out.println(response);
            if (response.startsWith("CREATE_ROOM_SUCCESS")) {
                if (response.split(" ").length == 2) {
                    String roomId = response.split(" ")[1];
                    this.roomId = roomId;
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

    private void deleteRoom(String userId) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(socket.getInputStream())) {

            // Send delete room request to the server
            out.println("DELETE_ROOM " + userId);

            // Read server response
            String response = in.nextLine();
            if (!"DELETE_ROOM_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Failed to delete room.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }
    }

    private void processMessage(String message) {
        if(message.startsWith("CHALLENGE_SENT")){
            System.out.println("tao o tren phan challenge file waitingroom");
        } else if (message.startsWith("CHALLENGE_FAIL1")) {
            JOptionPane.showMessageDialog(this, "Challenge failed. Please try again.");
        }

    }

}
