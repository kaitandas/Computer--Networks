package kaitan.com;

import javax.net.ssl.*;
import java.io.*;

public class SSLClient {
    public static void main(String[] args) throws Exception {

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 8443);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to secure server");

            // Handle server prompts
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("AUTH_")) break;
                out.println(console.readLine());
            }
        }
    }
}