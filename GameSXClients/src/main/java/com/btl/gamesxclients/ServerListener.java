package com.btl.gamesxclients;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerListener implements Runnable, AutoCloseable {
    private BufferedReader in;
    private Scanner scanner; // Dùng cho socket
    private GameScreen gameScreen;
    private WaitingRoomScreen waitingRoomScreen;

    // Constructor cho BufferedReader
    public ServerListener(BufferedReader in, GameScreen gameScreen) {
        this.in = in;
        this.gameScreen = gameScreen;
    }

    // Constructor cho Socket
    public ServerListener(Socket socket, WaitingRoomScreen waitingRoomScreen) {
        this.waitingRoomScreen = waitingRoomScreen;
        try {
            this.scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Nếu sử dụng BufferedReader
            if (in != null) {
                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(message);
                }
            }

            // Nếu sử dụng Scanner
            if (scanner != null) {
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    processMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        System.out.println("Nhận được từ server: " + message);  // Log để kiểm tra

        if (message.startsWith("CURRENT_SCORE:")) {
            String score = message.substring(14);
            gameScreen.updateScore(score);  // Cập nhật điểm trên giao diện
        } else if (message.startsWith("SCORE:")) {
            String result = message.substring(6);
            gameScreen.showResultMessage(result);  // Hiển thị kết quả đúng/sai
        } else if (message.startsWith("NUMBERS:")) {
            String[] numbers = message.substring(8).split(", ");
            gameScreen.updateServerNumbers(numbers);  // Hiển thị số cần sắp xếp
        } else if (message.startsWith("WORD:")) {
            String word = message.substring(5);
            gameScreen.updateServerWord(word);
        } else if (message.startsWith("CHALLENGE_REQUEST_FROM")) {
            // Xử lý yêu cầu thách đấu
            String challengerId = message.split(" ")[1]; // Giả sử format là "CHALLENGE_REQUEST_FROM [challengerId]"
            receiveChallenge(challengerId); // Gọi hàm xử lý thách đấu
        }
    }

    private void receiveChallenge(String challengerId) {
        System.out.println("Bạn đã nhận được yêu cầu thách đấu từ " + challengerId);
        // Commented out the JOptionPane logic for now
        // int response = JOptionPane.showConfirmDialog(this,
        //         "Người chơi " + challengerId + " đã thách đấu bạn. Bạn có chấp nhận không?",
        //         "Nhận Thách Đấu",
        //         JOptionPane.YES_NO_OPTION);
        //
        // if (response == JOptionPane.YES_OPTION) {
        //     sendChallengeResponse("ACCEPT_CHALLENGE");
        // } else {
        //     sendChallengeResponse("REJECT_CHALLENGE");
        // }
    }

    @Override
    public void close() {
        // Close resources if they are not null
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (scanner != null) {
            scanner.close(); // This will close the underlying InputStream of the socket as well
        }
    }
}
