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
                } else if (request.startsWith("GET_USER_PROFILE")) {
                    handleGetUserProfile(request);
                } else if (request.startsWith("JOIN_ROOM")) {
                    handleJoinRoom(request);
                } else if (request.startsWith("CREATE_ROOM")) {
                    handleCreateRoom(request);
                } else if (request.startsWith("DELETE_ROOM")){
                    handleDeleteRoom(request);
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
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT userid, password, ingame_name FROM users WHERE username = ?");
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        String userId = rs.getString("userid");
                        String ingameName = rs.getString("ingame_name");
                        if (storedPassword.equals(password)) {
                            out.println("LOGIN_SUCCESS " + userId + " " + ingameName);
                        } else {
                            out.println("LOGIN_FAIL");
                        }
                    } else {
                        out.println("LOGIN_FAIL");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("LOGIN_FAIL");
                }
            } else {
                out.println("LOGIN_FAIL");
            }
        }

        private void handleGetUserProfile(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                String userId = parts[1];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT ingame_name, totalpoint, rankedpoint FROM users WHERE userid = ?");
                    stmt.setString(1, userId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String ingameName = rs.getString("ingame_name");
                        int totalPoint = rs.getInt("totalpoint");
                        int rankedPoint = rs.getInt("rankedpoint");
                        out.println(ingameName + " " + totalPoint + " " + rankedPoint);
                    } else {
                        out.println("USER_NOT_FOUND");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("ERROR");
                }
            } else {
                out.println("INVALID_REQUEST");
            }
        }

        private void handleJoinRoom (String request) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String roomId = parts[1];
                String username = parts[2];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT player1_id, player2_id FROM rooms WHERE id = ?");
                    stmt.setString(1, roomId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String player1_id = rs.getString("player1_id");
                        String player2_id = rs.getString("player2_id");
                        if (player1_id != null && player2_id != null) {
                            out.println("ROOM_FULL");
                        } else if (player1_id == null) {
                            stmt = dbConnection.prepareStatement("UPDATE rooms SET player1_id = ? WHERE id = ?");
                            stmt.setString(1, username);
                            stmt.setString(2, roomId);
                            stmt.executeUpdate();
                            out.println("JOIN_SUCCESS");
                        } else {
                            stmt = dbConnection.prepareStatement("UPDATE rooms SET player2_id = ? WHERE id = ?");
                            stmt.setString(1, username);
                            stmt.setString(2, roomId);
                            stmt.executeUpdate();
                            out.println("JOIN_SUCCESS");
                        }
                    } else {
                        out.println("USER_NOT_FOUND");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("ERROR");
                }
            }
        }

        private void handleCreateRoom (String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                String userid = parts[1];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("INSERT INTO rooms (player1_id) VALUES (?)");
                    stmt.setString(1, userid);
                    stmt.executeUpdate();
                    PreparedStatement stmt2 = dbConnection.prepareStatement("SELECT id FROM rooms WHERE player1_id = ?");
                    stmt2.setString(1, userid);
                    ResultSet rs = stmt2.executeQuery();
                    if (rs.next()) {
                        out.println("CREATE_ROOM_SUCCESS " + rs.getString("id"));
                    } else {
                        out.println("CREATE_ROOM_FAIL");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("CREATE_ROOM_FAIL");
                }
            } else {
                out.println("CREATE_ROOM_FAIL");
            }
        }

        private void handleDeleteRoom (String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                String userid = parts[1];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM rooms WHERE player1_id = ? OR player2_id = ?");
                    stmt.setString(1, userid);
                    stmt.setString(2, userid);
                    stmt.executeUpdate();
                    out.println("DELETE_ROOM_SUCCESS");
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("DELETE_ROOM_FAIL");
                }
            } else {
                out.println("DELETE_ROOM_FAIL");
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