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
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private int score = 0;

        // Danh sách các từ để chơi
        private static final List<String> WORDS = Arrays.asList("apple", "banana", "orange", "grape", "mango");

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
                } else if (request.startsWith("DELETE_ROOM")) {
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
                username = in.readLine(); // Nhận tên người chơi

                // Bắt đầu game
                while (true) {
                    // Quyết định ngẫu nhiên giữa dãy số và từ
                    if (new Random().nextBoolean()) {
                        playNumberGame();
                    } else {
                        playWordGame();
                    }
            if (parts.length == 3) {
                String roomId = parts[1];
                String userId = parts[2];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT player1_id, player2_id FROM rooms WHERE id = ?");
                    stmt.setString(1, roomId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()){
                        String player1Id = rs.getString("player1_id");
                        String player2Id = rs.getString("player2_id");

                        if (player2Id == null) {
                            PreparedStatement stmt2 = dbConnection.prepareStatement("UPDATE rooms SET player2_id = ? WHERE id = ?");
                            stmt2.setString(1, userId);
                            stmt2.setString(2, roomId);
                            stmt2.executeUpdate();
                            out.println("JOIN_SUCCESS");
                            broadcast("PLAYER_JOIN");
                        } else {
                            out.println("ROOM_FULL");
                        }
                    } else {
                        out.println("JOIN_FAIL");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("ROOM_NOT_FOUND");
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

        private void playNumberGame() throws IOException {
            List<Integer> numbers = generateRandomNumbers();
            out.println("NUMBERS:" + numbers);  // Gửi dãy số cho client

            String sortedNumbers = in.readLine();  // Nhận dãy đã sắp xếp từ client
            List<Integer> userSorted = parseNumbers(sortedNumbers);

            if (isSorted(userSorted, numbers)) {
                score += 10;
                out.println("SCORE: Correct Answer +10 points");
            } else {
                score -= 5;
                out.println("SCORE: Wrong Answer -5 points");
            }
        }

        private void playWordGame() throws IOException {
            String word = WORDS.get(new Random().nextInt(WORDS.size())); // Chọn từ ngẫu nhiên
            String shuffledWord = shuffleWord(word); // Đảo vị trí từ
            out.println("WORD:" + shuffledWord);  // Gửi từ bị đảo cho client

            String userAnswer = in.readLine();  // Nhận từ đã sắp xếp từ client
            if (word.equals(userAnswer)) {
                score += 10;
                out.println("SCORE: Correct Answer +10 points");
            } else {
                score -= 5;
                out.println("SCORE: Wrong answer -5 points");
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

        private String shuffleWord(String word) {
            List<Character> characters = new ArrayList<>();
            for (char c : word.toCharArray()) {
                characters.add(c);
            }
            Collections.shuffle(characters);

        private void broadcast(String message) {
            System.out.println("Broadcasting message: " + message);
            for (ClientHandler client : clients) {
                client.out.println(message);
            StringBuilder shuffledWord = new StringBuilder();
            for (char c : characters) {
                shuffledWord.append(c);
            }
            return shuffledWord.toString();
        }
    }
}
