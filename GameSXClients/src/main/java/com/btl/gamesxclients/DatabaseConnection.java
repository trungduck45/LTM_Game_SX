/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.btl.gamesxclients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
   private static final String URL = "jdbc:mysql://localhost:3306/gamesx";  // Thay thế bằng URL của cơ sở dữ liệu
    private static final String USER = "root";  // Tên người dùng MySQL
    private static final String PASSWORD = "letrungduc45";  // Mật khẩu MySQL

    public void saveListNumWordToDatabase(int roomId, String listNumWord) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "UPDATE rooms SET message = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, listNumWord);
                statement.setInt(2, roomId);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("ListNumWord đã được lưu thành công vào cơ sở dữ liệu.");
                } else {
                    System.out.println("Không tìm thấy phòng với id: " + roomId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
     // Phương thức lấy dữ liệu từ cơ sở dữ liệu
    public String getListNumWordFromDatabase(int roomId) {
        String listNumWord = null; // Khởi tạo biến để lưu dữ liệu
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT message FROM rooms WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, roomId);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    listNumWord = resultSet.getString("message"); // Lấy dãy số hoặc dãy chữ từ cơ sở dữ liệu
                } else {
                    System.out.println("Không tìm thấy phòng với id: " + roomId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listNumWord; // Trả về dữ liệu lấy được
    }
}
