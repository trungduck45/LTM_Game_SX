
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
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gamesx", "root", "Minhhieu2003");

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
                System.out.println(request);
                if (request.startsWith("REGISTER")) {
                    handleRegister(request);
                } else if (request.startsWith("LOGIN")) {
                    handleLogin(request);
                } else if (request.startsWith("LOGOUT")) {
                    handleLogout(request);
                } else if (request.startsWith("GET_USER_PROFILE")) {
                    handleGetUserProfile(request);
                } else if (request.startsWith("JOIN_ROOM")) {
                    handleJoinRoom(request);
                } else if (request.startsWith("CREATE_ROOM")) {
                    handleCreateRoom(request);
                } else if (request.startsWith("DELETE_ROOM")) {
                    handleDeleteRoom(request);
                } else if (request.equals("GET_ALL_USERS")) { // Xử lý yêu cầu GET_ALL_USERS
                    handleGetAllUsers();
                } else if (request.startsWith("CHALLENGE")) {
                    handleChallenge(request);
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
                int totalpoint = 0;
                int rankedpoint = 0;
                String status = "offline";
                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("INSERT INTO users (username, password, ingame_name, totalpoint, rankedpoint, status) VALUES (?, ?, ?, ?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, ingameName);
                    stmt.setInt(4, totalpoint);
                    stmt.setInt(5, rankedpoint);
                    stmt.setString(6, status);
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
                    // Truy vấn username và kiểm tra mật khẩu cùng trạng thái
                    PreparedStatement stmt = dbConnection.prepareStatement(
                            "SELECT userid, password, ingame_name, status FROM users WHERE username = ?"
                    );
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        String userId = rs.getString("userid");
                        String ingameName = rs.getString("ingame_name");
                        String status = rs.getString("status");

                        if (storedPassword.equals(password)) {
                            if ("offline".equalsIgnoreCase(status)) {
                                // Cập nhật trạng thái thành online
                                PreparedStatement updateStatusStmt = dbConnection.prepareStatement(
                                        "UPDATE users SET status = 'online' WHERE username = ?"
                                );
                                updateStatusStmt.setString(1, username);
                                updateStatusStmt.executeUpdate();

                                this.username = username;
                                // Gửi phản hồi đăng nhập thành công
                                out.println("LOGIN_SUCCESS " + userId + " " + ingameName);
                            } else {
                                // Trả về lỗi nếu tài khoản đã được đăng nhập ở nơi khác
                                out.println("LOGIN_FAIL_2");
                            }
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

        private void handleLogout(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                String username = parts[1];

                try {
                    // Cập nhật trạng thái thành offline trong cơ sở dữ liệu
                    PreparedStatement updateStatusStmt = dbConnection.prepareStatement(
                            "UPDATE users SET status = 'offline' WHERE username = ?"
                    );
                    updateStatusStmt.setString(1, username);
                    int rowsUpdated = updateStatusStmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        // Gửi phản hồi đăng xuất thành công
                        out.println("LOGOUT_SUCCESS");
                    } else {
                        // Nếu không tìm thấy người dùng, trả về lỗi
                        out.println("LOGOUT_FAIL");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("LOGOUT_FAIL");
                }
            } else {
                out.println("LOGOUT_FAIL");
            }
        }

        private void handleGetUserProfile(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 2) {
                String userId = parts[1];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("SELECT ingame_name, totalpoint, rankedpoint, status FROM users WHERE userid = ?");
                    stmt.setString(1, userId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String ingameName = rs.getString("ingame_name");
                        int totalPoint = rs.getInt("totalpoint");
                        int rankedPoint = rs.getInt("rankedpoint");
                        String status = rs.getString("status");
                        out.println(ingameName + " " + totalPoint + " " + rankedPoint + " "+ status);
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
        private void handleGetAllUsers() {
            try {
                // Truy vấn để lấy tất cả người dùng
                PreparedStatement stmt = dbConnection.prepareStatement("SELECT userId, ingame_name, totalpoint, rankedpoint, status FROM users");
                ResultSet rs = stmt.executeQuery();

                // Gửi từng người chơi cho client, mỗi người chơi một dòng
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String ingameName = rs.getString("ingame_name");
                    int totalPoint = rs.getInt("totalpoint");
                    int rankedPoint = rs.getInt("rankedpoint");
                    String status = rs.getString("status");

                    // Định dạng và gửi thông tin của từng người chơi
                    out.println(userId + " " + ingameName + " " + totalPoint + " " + rankedPoint + " " + status);
                }
                // Gửi "END" để báo hiệu kết thúc danh sách
                out.println("END");

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("GET_ALL_USERS_FAIL");
            }
        }

        private void handleChallenge(String request) {
            String[] parts = request.split(" ");

            if (parts.length == 3) {
                String challengerName = parts[2];
                String targetUsername = parts[1];
                System.out.println("Target username: " + targetUsername);
                System.out.println("Challenger name: " + challengerName);

                System.out.println("List of connected users:");
                for (ClientHandler client : clients) {
                    System.out.println(client.username);
                }


                try {
                    boolean targetFound = false;
                    for (ClientHandler client : clients) {
                        if (client.username != null && client.username.equals(targetUsername)) {
                            targetFound = true;
                            System.out.println("Target found: " + client.username); // Kiểm tra target đã tìm thấy
                            if (client.socket.isConnected()) {
                                // Gửi yêu cầu thách đấu cho người chơi mục tiêu
                                System.out.println("Sending challenge to " + client.username);
                                client.out.println("CHALLENGE_REQUEST_FROM " + challengerName);
                                client.out.flush();
                                System.out.println("Challenge sent to " + targetUsername);
                                out.println("CHALLENGE_SENT " + targetUsername);
                            } else {
                                out.println("CHALLENGE_FAIL1 Target user is not online.");
                            }
                            break;
                        }
                    }
                    if (!targetFound) {
                        out.println("CHALLENGE_FAIL User not found.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("CHALLENGE_FAIL An error occurred.");
                }
            } else {
                out.println("CHALLENGE_FAIL Invalid request format.");
            }
        }



        private void startGame() throws IOException {
            StringBuilder message = new StringBuilder();
            int i=0;
            while( i<10){
                // Quyết định ngẫu nhiên giữa dãy số và từ
                if (new Random().nextBoolean()) {
                    message.append(generateRandomNumbers());
                } else {
                    message.append(shuffleWord());
                }
                message.append(";");
                i++;
            }
            message.setLength(message.length()-1);
            out.println("ListNumberAndWord:" + message );  // Gửi dãy số cho client
            System.out.println("message: "+ message );
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        private void playNumberGame() throws IOException {
//            List<Integer> numbers = generateRandomNumbers();
//
//
//            String sortedNumbers = in.readLine();  // Nhận dãy đã sắp xếp từ client
//            List<Integer> userSorted = parseNumbers(sortedNumbers);
//
////            if (isSorted(userSorted, numbers)) {
////                score += 10;
////                out.println("SCORE: Correct Answer +10 points");
////            } else {
////                score -= 5;
////                out.println("SCORE: Wrong Answer -5 points");
////            }
//        }

//        private void playWordGame() throws IOException {
//            String word = WORDS.get(new Random().nextInt(WORDS.size())); // Chọn từ ngẫu nhiên
//            String shuffledWord = shuffleWord(word); // Đảo vị trí từ
//            out.println("WORD:" + shuffledWord);  // Gửi từ bị đảo cho client
//
//            String userAnswer = in.readLine();  // Nhận từ đã sắp xếp từ client
//            if (word.equals(userAnswer)) {
//                score += 10;
//                out.println("SCORE: Correct Answer +10 points");
//            } else {
//                score -= 5;
//                out.println("SCORE: Wrong answer -5 points");
//            }
//        }

        private StringBuilder generateRandomNumbers() {
            Random random = new Random();
            int size = 3 + random.nextInt(8); // Kích thước ngẫu nhiên từ 3 đến 10

            StringBuilder numberString = new StringBuilder();
            for(int i=0; i<size ; i++){
                numberString.append(random.nextInt(100));
                numberString.append(",");
            }

            numberString.setLength(numberString.length()-1);

            return numberString;
        }
        private String shuffleWord() {
            String word = WORDS.get(new Random().nextInt(WORDS.size())); // Chọn từ ngẫu nhiên
            List<Character> characters = new ArrayList<>();
            for (char c : word.toCharArray()) {
                characters.add(c);
            }
            Collections.shuffle(characters);

            StringBuilder shuffledWord = new StringBuilder();
            for (char c : characters) {
                shuffledWord.append(c);
            }
            return shuffledWord.toString();
        }

        private void broadcast(String message) {
            System.out.println("Broadcasting message: " + message);
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
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

    }
}