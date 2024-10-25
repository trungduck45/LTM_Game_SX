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

       // out.println("Enter username:");
        username = in.readLine();

        // Bắt đầu game
        while (true) {
            List<Integer> numbers = generateRandomNumbers();
            out.println("Sort these numbers: " + numbers);

            String sortedNumbers = in.readLine();
            List<Integer> userSorted = parseNumbers(sortedNumbers);

            if (isSorted(userSorted, numbers)) {
                score += 10;
                out.println("Correct! +10 points.");
            } else {
                score -= 5;
                out.println("Wrong! -5 points.");
            }

            out.println("Your current score: " + score);
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

    private boolean isSorted(List<Integer> list,List<Integer> ListBanDau) {
       
        Collections.sort(ListBanDau);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != ListBanDau.get(i )) {
                    return false;
                }
            }
            return true;
        }

    private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }        
    }
}