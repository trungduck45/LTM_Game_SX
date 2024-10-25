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
                if (message.startsWith("Sort these numbers:")) {
                    String numbers = message.substring("Sort these numbers:".length()).trim();
                    gameScreen.updateServerNumbers(numbers.split(","));
                } else {
                    JOptionPane.showMessageDialog(gameScreen, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(gameScreen, "Mất kết nối với server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
