package kaitan.com;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.*;
import java.security.*;
import java.time.temporal.ChronoUnit;

public class SSLServer {
    private static final int PORT = 8443;
    private static final String KEYSTORE = "server.jks";
    private static final String KEYSTORE_PASS = "changeit";

    private static final Map<String, Instant> blockedIPs = new ConcurrentHashMap<>();
    private static Properties users = new Properties();

    // Helper method to convert bytes to hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Load user credentials
        try (InputStream is = Files.newInputStream(Paths.get("users.properties"))) {
            users.load(is);
        } catch (IOException e) {
            System.err.println("Failed to load user credentials: " + e.getMessage());
            return;
        }

        // SSL setup
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);

        try {
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            System.out.println("[SERVER] SSL server started on port " + PORT);

            ExecutorService pool = Executors.newFixedThreadPool(10);
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("[SERVER ERROR] " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final SSLSocket socket;

        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        public void run() {
            String ip = socket.getInetAddress().getHostAddress();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Check if IP is blocked
                Instant unblockTime = blockedIPs.get(ip);
                if (unblockTime != null && unblockTime.isAfter(Instant.now())) {
                    out.println("SERVER: Your IP is temporarily blocked");
                    return;
                }

                // Authentication
                out.println("USERNAME:");
                String username = in.readLine();

                out.println("PASSWORD:");
                String password = in.readLine();

                if (authenticate(username, password)) {
                    out.println("AUTH_SUCCESS");
                    logAuth(ip, username, true);
                } else {
                    out.println("AUTH_FAILED");
                    logAuth(ip, username, false);
                    blockIP(ip);
                }

            } catch (Exception e) {
                System.err.println("[CLIENT ERROR] " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("[SOCKET CLOSE ERROR] " + e.getMessage());
                }
            }
        }

        private boolean authenticate(String username, String password) {
            try {
                String storedHash = users.getProperty(username);
                if (storedHash == null) return false;

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String inputHash = bytesToHex(digest.digest(password.getBytes()));
                return storedHash.equals(inputHash);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Hash algorithm not found: " + e.getMessage());
                return false;
            }
        }

        private void blockIP(String ip) {
            blockedIPs.put(ip, Instant.now().plus(1, ChronoUnit.HOURS));
        }

        private void logAuth(String ip, String username, boolean success) {
            try {
                String logEntry = String.format("%s | %s | %s | %s%n",
                        Instant.now(), ip, username, success ? "SUCCESS" : "FAIL");
                Files.write(Paths.get("auth.log"), logEntry.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to write log: " + e.getMessage());
            }
        }
    }
}