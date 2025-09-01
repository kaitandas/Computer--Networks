package kaitan.com;

import java.net.*;
import java.io.*;
import java.security.*;

public class FileTransferClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            // Authentication
            System.out.println(in.readLine()); // USERNAME:
            out.println(console.readLine());
            System.out.println(in.readLine()); // PASSWORD:
            out.println(console.readLine());

            String authResponse = in.readLine();
            System.out.println(authResponse);
            if (!"AUTH_SUCCESS".equals(authResponse)) return;

            // File transfer
            System.out.println(in.readLine()); // COMMAND:
            String command = console.readLine();
            out.println(command);

            if ("UPLOAD".equalsIgnoreCase(command)) {
                uploadFile(in, out, console);
            } else if ("DOWNLOAD".equalsIgnoreCase(command)) {
                downloadFile(in, out, console);
            }
        }
    }

    private static void uploadFile(BufferedReader in, PrintWriter out, BufferedReader console) throws IOException {
        System.out.println(in.readLine()); // FILENAME:
        String filename = console.readLine();
        out.println(filename);

        System.out.println(in.readLine()); // FILE_DATA
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("ERROR: File '" + filename + "' not found in: " + file.getAbsolutePath());
            out.println("END"); // Send END to avoid server hanging
            return;
        }

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.println(line);
            }
            out.println("END");
            System.out.println(in.readLine()); // UPLOAD_SUCCESS
        }
    }

    private static void downloadFile(BufferedReader in, PrintWriter out, BufferedReader console) throws IOException {
        System.out.println(in.readLine()); // FILENAME:
        String filename = console.readLine();
        out.println(filename);

        String response = in.readLine();
        if (response.startsWith("ERROR")) {
            System.out.println(response);
            return;
        }

        try (PrintWriter fileWriter = new PrintWriter(filename)) {
            String line;
            while (!(line = in.readLine()).equals("FILE_END")) {
                fileWriter.println(line);
            }
        }
        System.out.println("DOWNLOAD_COMPLETE");
    }
}