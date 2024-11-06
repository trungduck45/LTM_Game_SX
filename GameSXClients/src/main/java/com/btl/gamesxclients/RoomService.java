package com.btl.gamesxclients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RoomService {

    public static Room getRoomById(String roomId) {
        try {
            Socket socket = new Socket("localhost", 12345); // Kết nối tới server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi yêu cầu lấy thông tin phòng với roomId cụ thể
            out.println("GET_ROOM " + roomId);

            // Đọc phản hồi từ server
            String response = in.readLine();

            if (response != null && !response.equals("NOT_FOUND")) {
                // Giả sử dữ liệu phòng bao gồm: roomId, player1Id, player2Id, roomStatus
                String[] roomData = response.split(" ");
                String receivedRoomId = roomData[0];
                String player1Id = roomData[1];
                String player2Id = roomData[2];
                String roomStatus = roomData[3];

                // Đóng kết nối
                in.close();
                out.close();
                socket.close();

                // Tạo và trả về đối tượng Room
                return new Room(receivedRoomId, player1Id, player2Id, roomStatus);
            } else {
                // Đóng kết nối
                in.close();
                out.close();
                socket.close();

                // Trả về null nếu không tìm thấy phòng
                System.out.println("Room with ID " + roomId + " not found.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
