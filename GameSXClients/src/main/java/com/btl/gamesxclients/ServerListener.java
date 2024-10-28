package com.btl.gamesxclients;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
public class ServerListener implements Runnable {
    private BufferedReader in;
    private GameScreen gameScreen;

    public ServerListener(BufferedReader in, GameScreen gameScreen) {
        this.in = in;
        this.gameScreen = gameScreen;
    }
@Override
public void run() {
    try {
        String message;
        while ((message = in.readLine()) != null) {
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
            } else if(message.startsWith("WORD:")){
                String word = message.substring(5);
                gameScreen.updateServerWord(word);
            }
            
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}

