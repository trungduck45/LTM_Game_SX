package com.btl.gamesxclients;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class ServerListener implements Runnable {
    private BufferedReader in;
    private CreateRoomScreen createRoomScreen;
    public ServerListener(BufferedReader in, CreateRoomScreen createRoomScreen) {
        this.in = in;
        this.createRoomScreen = createRoomScreen;
    }

@Override
public void run() {
    try {
        String message;
        while ((message = in.readLine()) != null) {
           // System.out.println("Nhận được từ server: " + message);  // Log để kiểm tra
            if (message.startsWith("ListNumberAndWord:")) {
                String List = message.substring(18);
                String[] parts = List.split("/");
                System.out.println(" nhân tu server listNumword:" + List);
                  // Lưu trực tiếp vào cơ sở dữ liệu
                DatabaseConnection dbClient;
                dbClient = new DatabaseConnection();
                int roomId = Integer.parseInt( parts[0]);
                String ListNumWord = parts[1];
                //System.out.println("roomid:"+ roomId+"-List:"+ListNumWord);
                dbClient.saveListNumWordToDatabase(roomId,ListNumWord);
            }

        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}

