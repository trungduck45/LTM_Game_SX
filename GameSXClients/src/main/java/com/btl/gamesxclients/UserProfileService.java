package com.btl.gamesxclients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserProfileService {
    public static UserProfile getUserProfile(String userId) {
        try {
            Socket socket = new Socket("localhost", 12345); // Connect to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send request to the server
            out.println("GET_USER_PROFILE " + userId);

            // Read the server's response
            String response = in.readLine();
            String[] profileData = response.split(" ");
            String ingameName = profileData[0];
            int totalPoint = Integer.parseInt(profileData[1]);
            int rankedPoint = Integer.parseInt(profileData[2]);
            String status = profileData[3];

            // Close resources
            in.close();
            out.close();
            socket.close();

            return new UserProfile(userId, ingameName, totalPoint, rankedPoint, status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static List<UserProfile> getAllPlayers() {
        List<UserProfile> playerList = new ArrayList<>();
        try {
            Socket socket = new Socket("localhost", 12345); // Kết nối tới server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi yêu cầu lấy danh sách người dùng
            out.println("GET_ALL_USERS");

            // Đọc phản hồi từ server, giả sử mỗi người chơi nằm trên một dòng
            String response;
            while ((response = in.readLine()) != null && !response.equals("END")) { 
                String[] profileData = response.split(" ");
                String userId = profileData[0];
                String ingameName = profileData[1];
                int totalPoint = Integer.parseInt(profileData[2]);
                int rankedPoint = Integer.parseInt(profileData[3]);
                String status = profileData[4];

                // Tạo đối tượng UserProfile và thêm vào danh sách
                UserProfile userProfile = new UserProfile(userId, ingameName, totalPoint, rankedPoint, status);
                playerList.add(userProfile);
            }

            // Đóng kết nối
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerList;
    }

    public static List<UserProfile> getOnlinePlayers() {
        List<UserProfile> onlinePlayerList = new ArrayList<>();
        try {
            Socket socket = new Socket("localhost", 12345); // Kết nối tới server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi yêu cầu lấy danh sách người dùng
            out.println("GET_ALL_USERS");

            // Đọc phản hồi từ server, giả sử mỗi người chơi nằm trên một dòng
            String response;
            while ((response = in.readLine()) != null && !response.equals("END")) {
                String[] profileData = response.split(" ");
                String userId = profileData[0];
                String ingameName = profileData[1];
                int totalPoint = Integer.parseInt(profileData[2]);
                int rankedPoint = Integer.parseInt(profileData[3]);
                String status = profileData[4];

                // Chỉ thêm người chơi có trạng thái "online"
                if ("online".equalsIgnoreCase(status)) {
                    // Tạo đối tượng UserProfile và thêm vào danh sách
                    UserProfile userProfile = new UserProfile(userId, ingameName, totalPoint, rankedPoint, status);
                    onlinePlayerList.add(userProfile);
                }
            }

            // Đóng kết nối
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return onlinePlayerList;
    }

}