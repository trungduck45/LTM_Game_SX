package com.btl.gamesxclients;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class GameScreen extends JFrame {
    private List<JButton> inputButtons = new ArrayList<>();
    private JPanel serverRow;
    private JPanel inputRow;
    private PrintWriter out;
    private BufferedReader in;
    private final String userId;

    private String DayCanSXSUM;
    private String DaycanSX;
    private String roomId;

    private JLabel scoreLabel;
    private int scoreSum = 0;

    private Timer timer; // Bộ đếm thời gian

    private JLabel timerLabel; // Hiển thị thời gian đếm ngược
    private int remainingTime; // Thời gian còn lại (tính bằng giây)

    private JLabel currentLevel;  // Màn hiện tại
    private JLabel ingameNameLabel;
    private JLabel ingameNameDThuLabel;

    private int currentLevelValue = 1;
    private final int MAX_LEVELS = 10;  // Số màn chơi tối đa

    private static final List<String> WORDS = Arrays.asList("a,p,p,l,e", "b,a,n,a,n,a", "o,r,a,n,g,e", "g,r,a,p,e", "m,a,n,g,o");

    private JButton firstSelectedButton = null;

    public GameScreen(String serverAddress, String userId, String roomId, String userIdDoiThu, String message) {
        this.userId = userId;
        try {
            Socket socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Dãy cần SX:" + message);
            this.roomId = roomId;
            initGameUI(userId, roomId, userIdDoiThu, message);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void initGameUI(String userId, String roomId, String userIdDoiThu, String question) {
        setTitle("Phòng chơi số " + roomId);
        setSize(650, 300);
        setLayout(new BorderLayout());
        // Fetch user profile
        UserProfile userProfile1 = UserProfileService.getUserProfile(userId);
        UserProfile userProfile2 = UserProfileService.getUserProfile(userIdDoiThu);

        currentLevel = new JLabel("Màn " + currentLevelValue, SwingConstants.CENTER);
        add(currentLevel, BorderLayout.NORTH);

        ingameNameLabel = new JLabel("Bạn: " + userProfile1.getIngameName());
        ingameNameLabel.setBorder(BorderFactory.createEmptyBorder(25, 25, 0, 25));
        ingameNameDThuLabel = new JLabel("Đối thủ: " + userProfile2.getIngameName());
        JPanel namePanel = new JPanel(new FlowLayout());
        namePanel.add(ingameNameLabel);
        namePanel.add(ingameNameDThuLabel);

        serverRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        serverRow.add(new JLabel("Server Row"));

        inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputRow.add(new JLabel("Input Row"));

        timerLabel = new JLabel("Thời gian: 20s", SwingConstants.CENTER);
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.add(timerLabel);

        scoreLabel = new JLabel("Điểm: " + scoreSum, SwingConstants.CENTER);
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scorePanel.add(scoreLabel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(namePanel);
        mainPanel.add(serverRow);
        mainPanel.add(inputRow);
        mainPanel.add(timerPanel);
        mainPanel.add(scorePanel);

        add(mainPanel, BorderLayout.CENTER);

        JButton exitButton = new JButton("Thoát Game");
        exitButton.addActionListener(e -> exitScreen());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        DayCanSXSUM = question;
        String[] ListNumWord = question.split(";");
        DaycanSX = ListNumWord[currentLevelValue - 1];

        if (DaycanSX.charAt(0) >= '0' && DaycanSX.charAt(0) <= '9') {
            String[] number = DaycanSX.split(",");
            updateServerNumbers(number);
        } else {
            updateServerWord(DaycanSX);
        }
    }

    private void increaseLevel() {
        if (currentLevelValue < MAX_LEVELS) {
            currentLevelValue++;
            currentLevel.setText("Màn " + currentLevelValue);
        } else {
            goToEndGameScreen(); // Chuyển sang trang EndGame khi chơi hết màn 10
        }
    }

    private void goToEndGameScreen() {
        dispose(); // Đóng cửa sổ GameScreen hiện tại
        EndGameScreen endGameScreen = new EndGameScreen(ingameNameLabel.getText().split(" ")[ingameNameLabel.getText().split(" ").length - 1], roomId, userId, scoreSum);
        endGameScreen.setVisible(true);
    }

    public void updateServerNumbers(String[] numbers) {
        // Xóa các phần tử cũ trước khi thêm mới
        serverRow.removeAll();
        inputRow.removeAll();
        inputButtons.clear(); // Xóa danh sách các ô nhập liệu cũ

        // Nối các số thành một chuỗi mà không có dấu ngoặc
        String numbersString = String.join(", ", numbers);

        // Tạo nhãn hiển thị chuỗi số
        JLabel serverLabel = new JLabel(numbersString);
        serverRow.add(serverLabel);

        // Thêm các nút tương ứng với mỗi số
        for (String number : numbers) {
            JButton inputButton = new JButton(number);
            inputButton.addActionListener(e -> handleButtonClick(inputButton));
            inputButtons.add(inputButton);
            inputRow.add(inputButton);
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
        inputButtons.clear(); // Xóa danh sách các ô nhập liệu cũ

        // Tạo nhãn hiển thị
        JLabel serverLabel = new JLabel(word);
        serverRow.add(serverLabel);

        // Thêm các nút tương ứng với mỗi ký tự
        for (char c : word.toCharArray()) {
            JButton inputButton = new JButton(String.valueOf(c));
            inputButton.addActionListener(e -> handleButtonClick(inputButton));
            inputButtons.add(inputButton);
            inputRow.add(inputButton);
        }

        // Cập nhật giao diện sau khi thêm các thành phần mới
        serverRow.revalidate();
        serverRow.repaint();
        inputRow.revalidate();
        inputRow.repaint();

        startTimer(); // Khởi động lại bộ đếm khi có câu mới
    }

    private void handleButtonClick(JButton clickedButton) {
        if (firstSelectedButton == null) {
            firstSelectedButton = clickedButton;
            firstSelectedButton.setFont(firstSelectedButton.getFont().deriveFont(Font.BOLD));
        } else {
            // Swap text of the two buttons
            String tempText = firstSelectedButton.getText();
            firstSelectedButton.setText(clickedButton.getText());
            clickedButton.setText(tempText);
            firstSelectedButton.setFont(firstSelectedButton.getFont().deriveFont(Font.PLAIN));
            firstSelectedButton = null;
        }
    }

    private void CheckDataAnswer() {
        if (timer != null) {
            timer.cancel(); // Hủy bộ đếm khi người dùng nhấn Check
        }
        try {
            StringBuilder inputData = new StringBuilder();
            // Lấy dữ liệu từ các nút
            for (JButton button : inputButtons) {
                String text = button.getText().trim();
                inputData.append(text).append(",");
            }
            // Xóa dấu phẩy cuối cùng
            if (inputData.length() > 0) {
                inputData.setLength(inputData.length() - 1);
            }
            String DayDaSX = String.valueOf(inputData);
            System.out.println("day ban dau: " + DaycanSX);
            System.out.println("cau tra loi: " + DayDaSX);
            int ok = 1;
            if (DaycanSX.charAt(0) >= '0' && DaycanSX.charAt(0) <= '9') {
                if (isSorted(ChuyenVeDayNumbers(DayDaSX), ChuyenVeDayNumbers(DaycanSX))) {
                    scoreSum += 10;
                } else {
                    scoreSum -= 5;
                }
            } else {
                for (String word : WORDS) {
                    if (DayDaSX.equals(word)) {
                        scoreSum += 10;
                        ok = 0;
                        break;
                    }
                }
                if (ok == 1) scoreSum -= 5;
            }
            if (currentLevelValue < MAX_LEVELS) {
                scoreLabel.setText("Điểm: " + scoreSum);
                String[] ListNumWord = DayCanSXSUM.split(";");
                DaycanSX = ListNumWord[currentLevelValue];

                if (DaycanSX.charAt(0) >= '0' && DaycanSX.charAt(0) <= '9') {
                    String[] number = DaycanSX.split(",");
                    updateServerNumbers(number);
                } else {
                    updateServerWord(DaycanSX);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi gửi dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        increaseLevel();
    }

    private void startTimer() {
        firstSelectedButton = null;
        if (timer != null) {
            timer.cancel(); // Hủy bộ đếm trước đó nếu có
        }
        remainingTime = 10; // Thời gian đếm ngược
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
                        CheckDataAnswer(); // Tự động gửi dữ liệu và chuyển sang câu tiếp theo
                    }
                });
            }
        }, 1000, 1000); // Thực hiện mỗi giây
        firstSelectedButton = null;
    }

    private void deleteRoom() {
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

    // Phương thức để quay về màn hình NameScreen
    private void exitScreen() {
        if (timer != null) {
            timer.cancel(); // Hủy bộ đếm trước đó nếu có
        }
        dispose(); // Đóng cửa sổ GameScreen
        // new WaitingRoomScreen(username, userId, ingameName); // Mở lại màn hình NameScreen
    }

    private boolean isSorted(List<Integer> list, List<Integer> originalList) {
        // Tạo bản sao của danh sách ban đầu để sắp xếp
        List<Integer> ascendingList = new ArrayList<>(originalList);
        List<Integer> descendingList = new ArrayList<>(originalList);

        // Sắp xếp bản sao theo thứ tự tăng dần và giảm dần
        Collections.sort(ascendingList);
        Collections.sort(descendingList, Collections.reverseOrder());

        // Kiểm tra xem danh sách đầu vào có khớp với bản sao đã sắp xếp không
        return list.equals(ascendingList) || list.equals(descendingList);
    }

    private List<Integer> ChuyenVeDayNumbers(String input) {
        List<Integer> numbers = new ArrayList<>();
        try {
            String[] parts = input.split(",");
            for (String part : parts) {
                numbers.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            out.println("ERROR: Invalid input. Please enter numbers separated by commas.");
        }
        return numbers;
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