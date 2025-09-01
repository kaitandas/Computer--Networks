package kaitan.com;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ThreadedServer {
    private static final int PORT = 8080;
    private static final int MAX_THREADS = 10; // Adjust as needed

    public static void main(String[] args) {
        // Thread pool to handle clients
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Multi-threaded server started on port " + PORT);
            System.out.println("[SERVER] Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("[SERVER] New connection from: " + clientIP);

                // Handle client in a separate thread
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] " + e.getMessage());
        } finally {
            threadPool.shutdown(); // Cleanup when server stops
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                out.println("SERVER: Connected! Send messages (type 'exit' to quit).");

                String msg;
                while ((msg = in.readLine()) != null && !msg.equalsIgnoreCase("exit")) {
                    System.out.println("[CLIENT " + clientSocket.getPort() + "] " + msg);
                    out.println("SERVER: Echo: '" + msg + "'");
                }

            } catch (IOException e) {
                System.err.println("[CLIENT HANDLER ERROR] " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("[SERVER] Client disconnected: " + clientSocket.getPort());
                } catch (IOException e) {
                    System.err.println("[SOCKET CLOSE ERROR] " + e.getMessage());
                }
            }
        }
    }
}