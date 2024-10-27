package com.btl.gamesxclients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

            // Close resources
            in.close();
            out.close();
            socket.close();

            return new UserProfile(userId, ingameName, totalPoint, rankedPoint);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}