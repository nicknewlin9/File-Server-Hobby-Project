package com.newlin.test;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SendingFileTest
{
    @Test
    public void Server()
    {
        int port = 12345;
        String filePath = "remote/Screen Recording 2023-01-29 at 11.23.26 AM.mov"; // Path to the large file to be sent

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     FileInputStream fileInputStream = new FileInputStream(filePath);
                     OutputStream outputStream = clientSocket.getOutputStream()) {

                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    System.out.println("File sent successfully.");

                } catch (IOException e) {
                    System.err.println("Error sending file: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    @Test
    public void Client()
    {
        String serverAddress = "localhost";
        int port = 12345;
        String saveFilePath = "local/Screen Recording 2023-01-29 at 11.23.26 AM.mov"; // Path where the received file will be saved

        try (Socket socket = new Socket(serverAddress, port);
             InputStream inputStream = socket.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath)) {

            System.out.println("Connected to server: " + serverAddress);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File received successfully.");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
