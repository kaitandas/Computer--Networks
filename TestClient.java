package kaitan.com;

import java.net.*;
import java.io.*;

import java.net.*;
import java.io.*;

public class TestClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080; // Match your server's port

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleIn = new BufferedReader(
                     new InputStreamReader(System.in))) {

            // Handle server prompts
            String serverPrompt;
            while ((serverPrompt = in.readLine()) != null) {
                System.out.println(serverPrompt);

                if (serverPrompt.startsWith("AUTH_")) break; // Exit on auth result
                if (serverPrompt.contains("username") || serverPrompt.contains("password")) {
                    String userInput = consoleIn.readLine(); // Wait for user input
                    out.println(userInput);
                }
            }

            // Post-auth communication
            if (in.readLine().contains("SUCCESS")) {
                System.out.println("Logged in! Type messages (or 'exit' to quit):");
                String userMsg;
                while (!(userMsg = consoleIn.readLine()).equalsIgnoreCase("exit")) {
                    out.println(userMsg);
                    System.out.println("Server: " + in.readLine());
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
