package kaitan.com;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;

public class FileTransferServer {
    private static final int PORT = 8080;
    private static final Map<String, String> users = Map.of(
            "admin", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f" // password123
    );

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] File Transfer Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Authentication
                out.println("USERNAME:");
                String username = in.readLine();
                out.println("PASSWORD:");
                String password = in.readLine();

                if (!authenticate(username, password)) {
                    out.println("AUTH_FAILED");
                    return;
                }
                out.println("AUTH_SUCCESS");

                // File transfer protocol
                out.println("COMMAND (UPLOAD/DOWNLOAD/EXIT):");
                String command = in.readLine();

                if ("UPLOAD".equalsIgnoreCase(command)) {
                    handleUpload(in, out);
                } else if ("DOWNLOAD".equalsIgnoreCase(command)) {
                    handleDownload(in, out);
                }

            } catch (IOException e) {
                System.err.println("[CLIENT ERROR] " + e.getMessage());
            }
        }

        private boolean authenticate(String username, String password) {
            String storedHash = users.get(username);
            return storedHash != null && storedHash.equals(hashPassword(password));
        }

        private String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes());
                return bytesToHex(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        private void handleUpload(BufferedReader in, PrintWriter out) throws IOException {
            out.println("FILENAME:");
            String filename = in.readLine();

            // Create server_files directory if not exists
            new File("server_files").mkdirs();

            out.println("FILE_DATA (END to finish):");
            try (FileOutputStream fos = new FileOutputStream("server_files/" + filename)) {
                String line;
                while (!(line = in.readLine()).equals("END")) {
                    fos.write((line + "\n").getBytes());
                }
            }
            out.println("UPLOAD_SUCCESS");
        }

        private void handleDownload(BufferedReader in, PrintWriter out) throws IOException {
            out.println("FILENAME:");
            String filename = in.readLine();

            try (BufferedReader fileReader = new BufferedReader(new FileReader("server_files/" + filename))) {
                out.println("FILE_START");
                String line;
                while ((line = fileReader.readLine()) != null) {
                    out.println(line);
                }
                out.println("FILE_END");
            } catch (FileNotFoundException e) {
                out.println("ERROR: File not found");
            }
        }
    }
}