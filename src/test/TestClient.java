package test;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Simple test client to verify server functionality
 */
public class TestClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean running;
    
    public TestClient(String username) {
        this.username = username;
        this.running = true;
    }
    
    public void connect() {
        try {
            System.out.println("Connecting to server at " + Constants.SERVER_HOST + ":" + Constants.SERVER_PORT);
            socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
            
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("✓ Connected to server!");
            
            // Send login message
            Message loginMsg = new Message(MessageType.LOGIN, username, "Login request");
            output.writeObject(loginMsg);
            output.flush();
            System.out.println("✓ Login request sent");
            
            // Start listener thread
            Thread listenerThread = new Thread(new MessageListener());
            listenerThread.start();
            
            // Start sending messages
            handleUserInput();
            
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              Test Client Commands                    ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ <message>           - Send public message            ║");
        System.out.println("║ @username <msg>     - Send private message           ║");
        System.out.println("║ /file @username     - Request file transfer          ║");
        System.out.println("║ /quit               - Disconnect                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
        
        while (running) {
            try {
                System.out.print(username + "> ");
                String input = scanner.nextLine();
                
                if (input.trim().isEmpty()) continue;
                
                if (input.equals("/quit")) {
                    disconnect();
                    break;
                }
                
                if (input.startsWith("@")) {
                    // Private message: @username message
                    String[] parts = input.substring(1).split(" ", 2);
                    if (parts.length >= 2) {
                        sendPrivateMessage(parts[0], parts[1]);
                    } else {
                        System.out.println("Usage: @username message");
                    }
                } else if (input.startsWith("/file @")) {
                    // File transfer: /file @username
                    String receiver = input.substring(7).trim();
                    requestFileTransfer(receiver);
                } else {
                    // Public message
                    sendPublicMessage(input);
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    private void sendPublicMessage(String content) {
        try {
            Message msg = new Message(MessageType.PUBLIC_MESSAGE, username, content);
            output.writeObject(msg);
            output.flush();
        } catch (Exception e) {
            System.err.println("Error sending public message: " + e.getMessage());
        }
    }
    
    private void sendPrivateMessage(String receiver, String content) {
        try {
            Message msg = new Message(MessageType.PRIVATE_MESSAGE, username, receiver, content);
            output.writeObject(msg);
            output.flush();
            System.out.println("→ Private message sent to " + receiver);
        } catch (Exception e) {
            System.err.println("Error sending private message: " + e.getMessage());
        }
    }
    
    private void requestFileTransfer(String receiver) {
        try {
            Message msg = new Message(MessageType.FILE_TRANSFER_REQUEST, username, receiver, 
                "test_file.txt");
            output.writeObject(msg);
            output.flush();
            System.out.println("→ File transfer request sent to " + receiver);
        } catch (Exception e) {
            System.err.println("Error requesting file transfer: " + e.getMessage());
        }
    }
    
    private void disconnect() {
        try {
            running = false;
            
            if (output != null) {
                Message logoutMsg = new Message(MessageType.LOGOUT, username, "Goodbye");
                output.writeObject(logoutMsg);
                output.flush();
            }
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            
            System.out.println("✓ Disconnected from server");
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    class MessageListener implements Runnable {
        @Override
        public void run() {
            try {
                while (running) {
                    Message msg = (Message) input.readObject();
                    displayMessage(msg);
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("Connection lost: " + e.getMessage());
                    running = false;
                }
            }
        }
        
        private void displayMessage(Message msg) {
            switch (msg.getType()) {
                case LOGIN_SUCCESS:
                    System.out.println("✓ " + msg.getContent());
                    break;
                    
                case LOGIN_FAILED:
                    System.out.println("✗ Login failed: " + msg.getContent());
                    disconnect();
                    break;
                    
                case PUBLIC_MESSAGE:
                    System.out.println("\n[PUBLIC] " + msg.getSender() + ": " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case PRIVATE_MESSAGE:
                    System.out.println("\n[PRIVATE from " + msg.getSender() + "]: " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case USER_JOINED:
                    System.out.println("\n[SYSTEM] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case USER_LEFT:
                    System.out.println("\n[SYSTEM] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case USER_LIST:
                    System.out.println("\n[ONLINE USERS] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case SERVER_ANNOUNCEMENT:
                    System.out.println("\n[SERVER ANNOUNCEMENT] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case FILE_TRANSFER_REQUEST:
                    System.out.println("\n[FILE TRANSFER] " + msg.getSender() + 
                        " wants to send you: " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case INFO:
                case SUCCESS:
                    System.out.println("\n[INFO] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                case ERROR:
                    System.out.println("\n[ERROR] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
                    
                default:
                    System.out.println("\n[" + msg.getType() + "] " + msg.getContent());
                    System.out.print(username + "> ");
                    break;
            }
        }
    }
    
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║            Test Client for Server                    ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty!");
                return;
            }
            
            TestClient client = new TestClient(username);
            client.connect();
        }
    }
}
