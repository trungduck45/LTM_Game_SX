package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GameScreen extends JFrame {
    private List<JTextField> inputFields = new ArrayList<>();
    private JPanel serverRow;
    private JPanel inputRow;
    private PrintWriter out;
    private BufferedReader in;
    
    private JLabel scoreLabel; 
    private int scoreSum = 0;
    
    private Timer timer; // Bộ đếm thời gian
    
    private JLabel timerLabel; // Hiển thị thời gian đếm ngược
    private int remainingTime; // Thời gian còn lại (tính bằng giây)
    
    private JLabel currentLevel ;  // Màn hiện tại
    private int currentLevelValue=1;
    private final int MAX_LEVELS = 10;  // Số màn chơi tối đa

    public GameScreen(String serverAddress, String playerName) {
        try {
            Socket socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(playerName); // Gửi tên người chơi tới server

            initGameUI();
            new Thread(new ServerListener(in, this)).start(); // Lắng nghe dữ liệu từ server

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
 private void initGameUI() {
        setTitle("Trò chơi sắp xếp");
        setSize(500, 300);
        setLayout(new BorderLayout());

        currentLevel = new JLabel("Màn " + currentLevelValue, SwingConstants.CENTER);
        timerLabel = new JLabel("Thời gian: 20s", SwingConstants.CENTER);
        scoreLabel = new JLabel("Điểm: 0", SwingConstants.CENTER);

        serverRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        serverRow.add(new JLabel("Server Row"));

        inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputRow.add(new JLabel("Input Row"));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.add(timerLabel);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scorePanel.add(scoreLabel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(currentLevel);
        mainPanel.add(serverRow);
        mainPanel.add(inputRow);
        mainPanel.add(timerPanel);
        mainPanel.add(scorePanel);

        add(mainPanel, BorderLayout.CENTER);

        JButton sendButton = new JButton("Check");
        sendButton.addActionListener(e -> sendDataToServer());

        JButton exitButton = new JButton("Thoát Game");
        exitButton.addActionListener(e -> exitToNameScreen());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void increaseLevel() {
        if (currentLevelValue < MAX_LEVELS) {
            currentLevelValue++;
            currentLevel.setText("Màn " + currentLevelValue);
        } else {
            
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            scorePanel.add(scoreLabel);
            // Hiển thị thông báo số điểm và thoát game
            
            String message = "Bạn đã hoàn thành tất cả các màn chơi!\nĐiểm của bạn: " + scoreSum ; // Giả định bạn có phương thức getScore() trả về điểm
            JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            // Gọi hàm thoát về trang namescreen
            exitToNameScreen();
        }
    }



    public void updateServerNumbers(String[] numbers) {
        // Xóa các phần tử cũ trước khi thêm mới
        serverRow.removeAll();
        inputRow.removeAll();
        inputFields.clear(); // Xóa danh sách các ô nhập liệu cũ

        // Nối các số thành một chuỗi mà không có dấu ngoặc
        String numbersString = String.join(", ", numbers);
        
        // Tạo nhãn hiển thị chuỗi số
        JLabel serverLabel = new JLabel(numbersString);
        serverRow.add(serverLabel);

        // Thêm các ô nhập liệu tương ứng với mỗi số
        for (String number : numbers) {
            JTextField inputField = new JTextField(2);
            inputFields.add(inputField);
            inputRow.add(inputField);
        }

        // Cập nhật giao diện sau khi thêm các thành phần mới
        serverRow.revalidate();
        serverRow.repaint();
        inputRow.revalidate();
        inputRow.repaint();
        
          startTimer(); // Khởi động lại bộ đếm khi có câu mới
    }
    
    public void updateServerWord(String word) {
        // Xóa các phần tử cũ trước khi thêm mới
        serverRow.removeAll();
        inputRow.removeAll();
        inputFields.clear(); // Xóa danh sách các ô nhập liệu cũ

      
        // Tạo nhãn hiển thị 
        JLabel serverLabel = new JLabel(word);
        serverRow.add(serverLabel);

        // Thêm các ô nhập liệu tương ứng với mỗi số
       
            JTextField inputField = new JTextField(10);
            inputFields.add(inputField);
            inputRow.add(inputField);
    

        // Cập nhật giao diện sau khi thêm các thành phần mới
        serverRow.revalidate();
        serverRow.repaint();
        inputRow.revalidate();
        inputRow.repaint();
        
        startTimer(); // Khởi động lại bộ đếm khi có câu mới
    }
    private void sendDataToServer() {
        if (timer != null) {
            timer.cancel(); // Hủy bộ đếm khi người dùng nhấn Check
        }
            
        try {
            StringBuilder inputData = new StringBuilder();

            // Lấy dữ liệu từ các ô nhập liệu
            for (JTextField field : inputFields) {
               
                String text = field.getText().trim();
                if(text.length() < 1){
                    text= "0";
                }
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                inputData.append(text).append(",");
            }

            // Xóa dấu phẩy cuối cùng
            if (inputData.length() > 0) {
                inputData.setLength(inputData.length() - 1);
            }

            // Gửi dữ liệu tới server
            out.println(inputData.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi gửi dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
         increaseLevel(); 
    }

    
    private void startTimer() {
        if (timer != null) {
            timer.cancel(); // Hủy bộ đếm trước đó nếu có
        }

        remainingTime = 21; // Thời gian đếm ngược 
        timerLabel.setText("Thời gian: " + remainingTime + "s"); // Cập nhật nhãn thời gian

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    remainingTime--;
                    timerLabel.setText("Thời gian: " + remainingTime + "s"); // Cập nhật giao diện

                    if (remainingTime <= 0) {
                        timer.cancel(); // Hủy bộ đếm khi hết thời gian
                        sendDataToServer(); // Tự động gửi dữ liệu và chuyển sang câu tiếp theo
                    }
                });
            }
        }, 1000, 1000); // Thực hiện mỗi giây
    }


    // Phương thức để quay về màn hình NameScreen
    private void exitToNameScreen() {
         if (timer != null) {
            timer.cancel(); // Hủy bộ đếm trước đó nếu có
        }
        dispose(); // Đóng cửa sổ GameScreen
        new NameScreen(); // Mở lại màn hình NameScreen
    }
public void updateScore(String score) {
        SwingUtilities.invokeLater(() -> scoreLabel.setText("Điểm: " + score));
        scoreSum = Integer.parseInt(score);
    }

    
public void showResultMessage(String message) {
    SwingUtilities.invokeLater(() -> {
        // Tạo JDialog để hiển thị thông báo
        JDialog dialog = new JDialog(this, "Thông báo", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
        dialog.setSize(300, 100); // Kích thước hộp thoại
        dialog.setLocationRelativeTo(this); // Căn giữa so với cửa sổ chính
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Sử dụng java.util.Timer để đóng JDialog sau 1 giây
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(dialog::dispose); // Đóng hộp thoại trên Swing EDT
            }
        }, 1000); // 1000ms = 1 giây

        // Hiển thị hộp thoại
        dialog.setVisible(true);
    });
}
}
