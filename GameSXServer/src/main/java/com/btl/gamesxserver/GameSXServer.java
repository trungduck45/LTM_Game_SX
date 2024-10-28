package com.btl.gamesxserver;

import java.net.*;
import java.io.*;
import java.util.*;

public class GameSXServer {

    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

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

                username = in.readLine(); // Nhận tên người chơi

                // Bắt đầu game
                while (true) {
                    // Quyết định ngẫu nhiên giữa dãy số và từ
                    if (new Random().nextBoolean()) {
                        playNumberGame();
                    } else {
                        playWordGame();
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

            StringBuilder shuffledWord = new StringBuilder();
            for (char c : characters) {
                shuffledWord.append(c);
            }
            return shuffledWord.toString();
        }
    }
}
