package com.btl.gamesxclients;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerListScreen extends JFrame {

    public PlayerListScreen() {
        setTitle("Danh sách người chơi");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Tạo mô hình bảng và JTable
        String[] columnNames = {"User Name", "Total Point", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable playerTable = new JTable(tableModel);
        playerTable.setFillsViewportHeight(true);
        playerTable.setRowHeight(30);
        playerTable.setFont(new Font("Arial", Font.PLAIN, 14));
        playerTable.setShowGrid(true);
        playerTable.setGridColor(Color.LIGHT_GRAY);

        // Tùy chỉnh tiêu đề bảng
        JTableHeader tableHeader = playerTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 16));
        tableHeader.setBackground(Color.GRAY);
        tableHeader.setForeground(Color.WHITE);

        // Lấy danh sách người chơi
        List<UserProfile> players = UserProfileService.getAllPlayers(); // Giả sử phương thức này trả về danh sách người chơi

        // Sắp xếp danh sách theo total point giảm dần
        Collections.sort(players, new Comparator<UserProfile>() {
            @Override
            public int compare(UserProfile p1, UserProfile p2) {
                return Integer.compare(p2.getTotalPoint(), p1.getTotalPoint()); // Sắp xếp giảm dần
            }
        });

        // Đưa dữ liệu người chơi vào bảng
        for (UserProfile player : players) {
            Object[] rowData = {player.getIngameName(), player.getTotalPoint(), player.getStatus()};
            tableModel.addRow(rowData);
        }

        // Thêm bảng vào trong một JScrollPane
        JScrollPane scrollPane = new JScrollPane(playerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Tạo nút "Quay lại" và xử lý sự kiện khi nhấn nút
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Đóng cửa sổ danh sách người chơi
            }
        });

        // Thêm nút vào panel phía dưới
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Hiển thị cửa sổ ở giữa màn hình
        setLocationRelativeTo(null);
        setVisible(true);
    }
}