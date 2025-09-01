package kaitan.com;

import java.net.*;
import java.io.*;

public class BasicServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Waiting for clients...");

            while (true) {  // Add this loop to keep server running
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New client connected");

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    // Send welcome message
                    out.println("SERVER: Connected! Send me a message.");

                    // Read client message
                    String msg = in.readLine();
                    System.out.println("[CLIENT] " + msg);

                    // Reply
                    out.println("SERVER: Received your message: '" + msg + "'");

                } catch (IOException e) {
                    System.err.println("[CLIENT ERROR] " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }
}