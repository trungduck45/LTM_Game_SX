/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.btl.gamesxserver;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
/**
 *
 * @author letru
 */
public class GameSXServer {

    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private int score = 0;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

@Override
public void run() {
    try {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        username = in.readLine(); // Nhận tên người chơi

        // Bắt đầu game
        while (true) {
            List<Integer> numbers = generateRandomNumbers();
            out.println("NUMBERS:" + numbers);  // Thông báo số cần sắp xếp

            String sortedNumbers = in.readLine();
            List<Integer> userSorted = parseNumbers(sortedNumbers);

            if (isSorted(userSorted, numbers)) {
                score += 10;
                out.println("SCORE: Correct Answer +10 points");
            } else {
                score -= 5;
                out.println("SCORE: Wrong answer -5 points");
            }

            // Gửi điểm hiện tại cho client
            out.println("CURRENT_SCORE:" + score);
        }

    } catch (IOException e) {
        System.out.println("Client disconnected: " + socket.getInetAddress());
    } finally {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    private List<Integer> generateRandomNumbers() {
        Random random = new Random();
        int size = 3 + random.nextInt(8); // Kích thước ngẫu nhiên từ 3 đến 10
        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            numbers.add(random.nextInt(100)); // Giá trị ngẫu nhiên từ 0 đến 99
        }

        return numbers;
    }

    private List<Integer> parseNumbers(String input) {
        List<Integer> numbers = new ArrayList<>();
            String[] parts = input.split(",");
            for (String part : parts) {
                numbers.add(Integer.parseInt(part.trim()));
            }
            return numbers;
        }      

    private boolean isSorted(List<Integer> list, List<Integer> originalList) {
    // Tạo bản sao của danh sách ban đầu để sắp xếp
    List<Integer> ascendingList = new ArrayList<>(originalList);
    List<Integer> descendingList = new ArrayList<>(originalList);

    // Sắp xếp bản sao theo thứ tự tăng dần và giảm dần
    Collections.sort(ascendingList);
    Collections.sort(descendingList, Collections.reverseOrder());

    // Kiểm tra xem danh sách đầu vào có khớp với bản sao đã sắp xếp không
    if (list.equals(ascendingList)) {
        return true;  // Dãy số được sắp xếp tăng dần
    } else if (list.equals(descendingList)) {
        return true;  // Dãy số được sắp xếp giảm dần
    } else {
        return false; // Dãy số không được sắp xếp
    }
}


    private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }        
    }
}