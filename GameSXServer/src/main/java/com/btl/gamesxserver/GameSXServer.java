
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
    private static int check=1;
    private static String roomIdplay;
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
                } else if (request.equals("GET_ALL_USERS")) {
                    handleGetAllUsers();
                } else if (request.startsWith("START_GAME")) {
                    //System.out.println("tao danh sach:..........");
                    startGame(request);
                } else if (request.startsWith("GET_ROOM")) {
                    handleGetRoom(request);
                } else if (request.startsWith("SEND_SCORE")) {
                    handleSendScore(request);
                } else {
                    username = request;
                }

            } catch (IOException  e) {
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

        private void handleSendScore(String request) {
            String[] parts = request.split(" ");
            if (parts.length == 4) {
                String roomId = parts[1];
                String userId = parts[2];
                String score = parts[3]; // Store score as a string
                try {
                    // Query the current state of `score_1` and `score_2` for the specified room
                    PreparedStatement checkStmt = dbConnection.prepareStatement(
                            "SELECT player1_id, player2_id FROM rooms WHERE id = ?"
                    );
                    checkStmt.setInt(1, Integer.parseInt(roomId));
                    ResultSet rs = checkStmt.executeQuery();

                    String player1_id = "";
                    String player2_id = "";

                    if (rs.next()) {
                        player1_id = rs.getString("player1_id");
                        player2_id = rs.getString("player2_id");
                        if (player1_id.equals(userId)) {
                            PreparedStatement updateStmt = dbConnection.prepareStatement(
                                    "UPDATE rooms SET score_1 = ? WHERE id = ?"
                            );
                            updateStmt.setString(1, score);
                            updateStmt.setInt(2, Integer.parseInt(roomId));
                            updateStmt.executeUpdate();
                        } else if (player2_id.equals(userId)) {
                            PreparedStatement updateStmt = dbConnection.prepareStatement(
                                    "UPDATE rooms SET score_2 = ? WHERE id = ?"
                            );
                            updateStmt.setString(1, score);
                            updateStmt.setInt(2, Integer.parseInt(roomId));
                            updateStmt.executeUpdate();
                        } else {
                            System.out.println("User not in room with ID: " + roomId);
                            out.println("SCORE_FAIL User not in room");
                            return;
                        }
                    }

                    while (true) {
                        PreparedStatement stmt = dbConnection.prepareStatement("SELECT score_1, score_2 FROM rooms WHERE id = ?");
                        stmt.setInt(1, Integer.parseInt(roomId));
                        ResultSet rs1 = stmt.executeQuery();
                        if (rs1.next()) {
                            String Score1 = rs1.getString("score_1");
                            String Score2 = rs1.getString("score_2");
                            if (!Score1.isEmpty() && !Score2.isEmpty()) {
                                String res = "RESULT " + roomId + " " + player1_id + " " + player2_id + " " + Score1 + " " + Score2;
                                System.out.println(res);
                                broadcast(res);
                                break;
                            }
                        }
                    }

//                    if (rs.next()) {
//                        String currentScore1 = rs.getString("score_1");
//                        String currentScore2 = rs.getString("score_2");
//
//                        if (currentScore1 == null || currentScore1.isEmpty()) {
//                            // If score_1 is empty, insert the score there
//                            PreparedStatement updateStmt = dbConnection.prepareStatement(
//                                    "UPDATE rooms SET score_1 = ? WHERE id = ?"
//                            );
//                            updateStmt.setString(1, score);
//                            updateStmt.setInt(2, Integer.parseInt(roomId));
//                            updateStmt.executeUpdate();
//                            out.println("SCORE_SUCCESS: 1 player complete");
//                        } else if (currentScore2 == null || currentScore2.isEmpty()) {
//                            // If score_1 is filled, insert into score_2
//                            PreparedStatement updateStmt = dbConnection.prepareStatement(
//                                    "UPDATE rooms SET score_2 = ? WHERE id = ?"
//                            );
//                            updateStmt.setString(1, score);
//                            updateStmt.setInt(2, Integer.parseInt(roomId));
//                            updateStmt.executeUpdate();
//                            out.println("SCORE_SUCCESS: 2 player complete");
//                            System.out.println("send: "+ "SCORE_SUCCESS: 2 player complete");
//
//                            compareScoresIfReady(Integer.parseInt(roomId));
//                        } else {
//                            System.out.println("Both scores are already filled for room ID: " + roomId);
//                            out.println("SCORE_FAIL Both scores are already filled");
//                            return;
//                        }
//
//                        System.out.println("Score saved for userId: " + userId + " in roomId: " + roomId);
//
//                    } else {
//                        System.out.println("Room not found with ID: " + roomId);
//                        out.println("SCORE_FAIL Room not found");
//                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("SCORE_FAIL");
                }
            } else {
                System.out.println("Invalid SEND_SCORE request format");
                out.println("SCORE_FAIL");
            }
        }

        private void compareScoresIfReady(int roomId) {
            try {
                // Check if both scores are set
                PreparedStatement checkStmt = dbConnection.prepareStatement(
                        "SELECT score_1, score_2 FROM rooms WHERE id = ?"
                );
                checkStmt.setInt(1, roomId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String currentScore1 = rs.getString("score_1");
                    String currentScore2 = rs.getString("score_2");

                    if (currentScore1 != null && !currentScore1.isEmpty() &&
                            currentScore2 != null && !currentScore2.isEmpty()) {
                        // Both scores are present, now compare them
                        String[] score1Parts = currentScore1.split("/");
                        String[] score2Parts = currentScore2.split("/");

                        if (score1Parts.length == 2 && score2Parts.length == 2) {
                            int score1Value = Integer.parseInt(score1Parts[1]);
                            int score2Value = Integer.parseInt(score2Parts[1]);

                            String comparisonResult;
                            if (score1Value > score2Value) {
                                comparisonResult = "User " + score1Parts[0] + " has a higher score.";
                            } else if (score1Value < score2Value) {
                                comparisonResult = "User " + score2Parts[0] + " has a higher score.";
                            } else {
                                comparisonResult = "Both users have equal scores.";
                            }

                            // Notify clients of the result
                            out.println("SCORE_COMPARISON " + comparisonResult);
                            System.out.println("send to client: "+"SCORE_COMPARISON " + comparisonResult);
                        } else {
                            System.out.println("Score format error for room ID: " + roomId);
                            out.println("SCORE_FAIL Invalid score format");
                        }
                    }
                }
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                out.println("SCORE_FAIL");
            }
        }



        private void handleGetRoom(String request) {
            // Lấy roomId từ request
            String[] parts = request.split(" ");
            if (parts.length < 2) {
                out.println("ERROR: Room ID required");
                return;
            }
            String roomId = parts[1];

            try {
                // Truy vấn cơ sở dữ liệu để lấy thông tin phòng dựa trên roomId
                String query = "SELECT player1_id, player2_id, message FROM rooms WHERE id = ?";
                PreparedStatement statement = dbConnection.prepareStatement(query);
                statement.setString(1, roomId);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Lấy thông tin phòng và trả về cho client
                    String player1Id = resultSet.getString("player1_id");
                    String player2Id = resultSet.getString("player2_id");
                    String message = resultSet.getString("message");

                    out.println("ROOM_INFO: " + roomId + " " + player1Id + " " + player2Id +" "+ message);
                    System.out.println("ROOM_INFO:" + roomId + " " + player1Id + " " + player2Id + " " + message);
                } else {
                    out.println("ERROR: Room not found");
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("ERROR: Database error");
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

        private void handleJoinRoom (String request) throws IOException {
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
                            startGame("START_GAME " + roomId);

                            broadcast("PLAYER_JOIN");
                            //handleGetRoom("GET_ROOM "+roomId);
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
            String message="";
            String score1 ="";
            String score2 ="";
            if (parts.length == 2) {
                String userid = parts[1];

                try {
                    PreparedStatement stmt = dbConnection.prepareStatement("INSERT INTO rooms (player1_id,message,score_1,score_2) VALUES (?,?,?,?)");
                    stmt.setString(1, userid);
                    stmt.setString(2, message);
                    stmt.setString(3, score1);
                    stmt.setString(4, score2);
                    stmt.executeUpdate();
                    PreparedStatement stmt2 = dbConnection.prepareStatement("SELECT id FROM rooms WHERE player1_id = ?");
                    stmt2.setString(1, userid);
                    ResultSet rs = stmt2.executeQuery();
                    if (rs.next()) {
                        out.println("CREATE_ROOM_SUCCESS " + rs.getString("id"));
                    } else {
                        out.println("CREATE_ROOM_FAIL");
                    }
                     roomIdplay = rs.getString("id");
                     System.out.println("roomid play:" +roomIdplay);
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

        private void startGame(String request) throws IOException {
            // Lấy roomId từ request
            String[] parts = request.split(" ");
            if (parts.length < 2) {
                out.println("ERROR: Room ID required");
                return;
            }
            String roomId = parts[1];
            StringBuilder message = new StringBuilder();
            int i=0;
            while( i<10){
                if (new Random().nextBoolean()) {
                    message.append(generateRandomNumbers());
                } else {
                    message.append(shuffleWord());
                }
                message.append(";");
                i++;
            }
            message.setLength(message.length()-1);
            System.out.println("create message : "+ message );
            try{
                PreparedStatement stmt2 = dbConnection.prepareStatement("UPDATE rooms SET message = ? WHERE id = ?");
                stmt2.setString(1, String.valueOf(message));
                stmt2.setString(2, roomId);
                stmt2.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("ROOM_NOT_FOUND");
            }

            handleGetRoom("GET_ROOM "+roomId);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
    }
}