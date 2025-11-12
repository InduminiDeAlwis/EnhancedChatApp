package client;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean running = false;
    
    public Client(String username) {
        this.username = username;
    }
    
    public void connect() {
        try {
            socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Send login message
            Message loginMsg = new Message(MessageType.LOGIN, username, username);
            output.writeObject(loginMsg);
            output.flush();
            
            // Wait for login response
            Message response = (Message) input.readObject();
            
            if (MessageType.ERROR.equals(response.getType())) {
                System.out.println("Login failed: " + response.getContent());
                disconnect();
                return;
            }
            
            System.out.println("✓ Connected to server as: " + username);
            running = true;
            
            // Start listener thread
            ClientListener listener = new ClientListener(input);
            new Thread(listener).start();
            
            // Start command loop
            handleUserInput();
            
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            disconnect();
        }
    }
    
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nCommands:");
        System.out.println("  /msg <user> <message> - Send private message");
        System.out.println("  /quit - Disconnect");
        System.out.println("  Just type to send public message\n");
        
        while (running) {
            try {
                String input = scanner.nextLine();
                
                if (input.startsWith("/quit")) {
                    disconnect();
                    break;
                } else if (input.startsWith("/msg ")) {
                    String[] parts = input.substring(5).split(" ", 2);
                    if (parts.length == 2) {
                        sendPrivateMessage(parts[0], parts[1]);
                    } else {
                        System.out.println("Usage: /msg <username> <message>");
                    }
                } else if (!input.trim().isEmpty()) {
                    sendPublicMessage(input);
                }
                
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
    
    private void sendPublicMessage(String content) {
        try {
            Message msg = new Message(MessageType.PUBLIC_MESSAGE, username, content);
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
    
    private void sendPrivateMessage(String receiver, String content) {
        try {
            Message msg = new Message(MessageType.PRIVATE_MESSAGE, username, receiver, content);
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            System.err.println("Failed to send private message: " + e.getMessage());
        }
    }
    
    private void disconnect() {
        try {
            running = false;
            
            if (output != null) {
                Message logoutMsg = new Message(MessageType.LOGOUT, username, "");
                output.writeObject(logoutMsg);
                output.flush();
            }
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            System.out.println("Disconnected from server");
            System.exit(0);
            
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }
        
        Client client = new Client(username);
        client.connect();
    }
}
