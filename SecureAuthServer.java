package kaitan.com;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.security.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecureAuthServer {
    private static final int PORT = 8080;
    private static final int MAX_THREADS = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static volatile boolean isRunning = true;

    // Store failed attempts per IP
    private static ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();

    // Password hashing (SHA-256)
    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Secure auth server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();

                // Check if IP is blocked
                if (failedAttempts.getOrDefault(clientIP, 0) >= MAX_ATTEMPTS) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("SERVER: Your IP is blocked. Too many failed attempts.");
                    clientSocket.close();
                    continue;
                }

                threadPool.execute(new AuthHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    static class AuthHandler implements Runnable {
        private final Socket clientSocket;

        public AuthHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String clientIP = clientSocket.getInetAddress().getHostAddress();

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Authentication phase
                out.println("SERVER: Enter username:");
                String username = in.readLine();
                out.println("SERVER: Enter password:");
                String password = in.readLine();

                // Verify credentials (demo: hardcoded "admin/password123")
                if ("admin".equals(username) &&
                        "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f".equals(hashPassword(password))) {

                    out.println("AUTH_SUCCESS");
                    System.out.println("[AUTH] " + clientIP + " logged in as " + username);

                    // Handle authenticated session
                    while (true) {
                        String msg = in.readLine();
                        if ("exit".equalsIgnoreCase(msg)) break;
                        out.println("SERVER: Echo: " + msg);
                    }

                } else {
                    // Track failed attempts
                    failedAttempts.merge(clientIP, 1, Integer::sum);
                    out.println("AUTH_FAILED (Attempts left: " +
                            (MAX_ATTEMPTS - failedAttempts.get(clientIP)));
                }

            } catch (Exception e) {
                System.err.println("[AUTH ERROR] " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("[SOCKET CLOSE ERROR] " + e.getMessage());
                }
            }
        }
    }
}