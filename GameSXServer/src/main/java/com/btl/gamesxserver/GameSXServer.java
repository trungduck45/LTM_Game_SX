package com.btl.gamesxserver;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class GameSXServer {

    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Connection dbConnection;

    public static void main(String[] args) {
        try {
            // Initialize database connection
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gamesx", "root", "dong1808");

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
        } catch (SQLException e) {
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

                String request = in.readLine();
                if (request.startsWith("REGISTER")) {
                    handleRegister(request);
                } else if (request.startsWith("LOGIN")) {
                    handleLogin(request);
                } else if (request.startsWith("CREATE_ROOM")) {
                    handleCreateRoom(request);
                } else {
                    // Handle other requests (e.g., game logic)
                    username = request;
                    startGame();
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

        private void handleRegister(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 4) {
                String username = parts[1];
                String password = parts[2];
                String ingameName = parts[3];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("INSERT INTO users (username, password, ingame_name) VALUES (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, ingameName);
                    stmt.executeUpdate();
                    out.println("REGISTER_SUCCESS");
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("REGISTER_FAIL");
                }
            } else {
                out.println("REGISTER_FAIL");
            }
        }

        private void handleLogin(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT password FROM users WHERE username = ?");
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (storedPassword.equals(password)) {
                            out.println("SUCCESS");
                        } else {
                            out.println("FAIL");
                        }
                    } else {
                        out.println("FAIL");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("FAIL");
                }
            } else {
                out.println("FAIL");
            }
        }

        private void handleCreateRoom(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                int player1Id = Integer.parseInt(parts[1]);

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("INSERT INTO rooms (player1_id, player2_id) VALUES (?, NULL)");
                    stmt.setInt(1, player1Id);
                    stmt.executeUpdate();
                    out.println("ROOM_CREATED");
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("ROOM_CREATION_FAILED");
                }
            } else {
                out.println("ROOM_CREATION_FAILED");
            }
        }

        private void startGame() throws IOException {
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

        private boolean isSorted(List<Integer> list, List<Integer> ListBanDau) {
            Collections.sort(ListBanDau);
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).equals(ListBanDau.get(i))) {
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