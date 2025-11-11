package client;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.Socket;

/**
 * Client.java
 * Main client class that connects to server and handles communication
 * This is the networking layer - UI classes will use this to send/receive messages
 */
public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientListener listener;
    private String username;
    private boolean connected;

    /**
     * Constructor
     */
    public Client() {
        this.connected = false;
    }

    /**
     * Connect to server
     * @return true if connection successful
     */
    public boolean connect(String host, int port) {
        try {
            // Create socket connection to server
            socket = new Socket(host, port);
            connected = true;

            // Set up I/O streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server: " + host + ":" + port);

            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * Login with username
     * @return true if login successful
     */
    public boolean login(String username) throws IOException {
        if (!connected) {
            return false;
        }

        // Wait for server's "Enter username:" prompt
        String serverPrompt = in.readLine();
        System.out.println("Server: " + serverPrompt);

        // Send username to server
        out.println(username);
        out.println(username);

        // Server sends multiple messages: join broadcast, then welcome, then user list
        // Read up to 3 messages to find the welcome message
        for (int i = 0; i < 3; i++) {
            String response = in.readLine();

            if (response == null) {
                System.err.println("No response from server (attempt " + (i+1) + ")");
                if (i == 2) return false; // Give up after 3 tries
                continue;
            }

            System.out.println("Server response " + (i+1) + ": " + response);

            Message responseMsg = Message.fromProtocol(response);

            if (responseMsg == null) {
                System.err.println("Invalid message format, trying next...");
                continue; // Try next line
            }

            // Check for error first
            if (responseMsg.getType() == MessageType.ERROR) {
                System.err.println("Login failed: " + responseMsg.getContent());
                return false;
            }

            // Check for welcome message (login success)
            if (responseMsg.getType() == MessageType.SYSTEM &&
                responseMsg.getContent().contains("Welcome")) {
                this.username = username;
                System.out.println("✓ Logged in successfully as: " + username);
                return true;
            }

            // Otherwise it might be join broadcast or user list, keep reading
            System.out.println("Not a welcome message, reading next...");
        }

        System.err.println("Login failed: Did not receive welcome message after 3 attempts");
        return false;
    }


    /**
     * Start listening for messages from server
     * This should be called after successful login
     */
    public void startListening(MessageHandler handler) {
        if (listener != null && listener.isAlive()) {
            return; // Already listening
        }

        listener = new ClientListener(in, handler);
        listener.start();
        System.out.println("Started listening for messages");
    }

    /**
     * Send broadcast message to all users
     */
    public void sendBroadcastMessage(String content) {
        Message message = new Message(MessageType.BROADCAST, username, content);
        sendMessage(message);
    }

    /**
     * Send private message to specific user
     * This is part of your private chat feature!
     */
    public void sendPrivateMessage(String recipient, String content) {
        Message message = new Message(MessageType.PRIVATE, username, recipient, content);
        sendMessage(message);
        System.out.println("Sent private message to " + recipient + ": " + content);
    }

    /**
     * Request list of online users
     */
    public void requestUserList() {
        Message message = new Message(MessageType.USER_LIST, username, "");
        sendMessage(message);
    }

    /**
     * Send any message to server
     */
    private void sendMessage(Message message) {
        if (out != null && connected) {
            out.println(message.toProtocol());
        }
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            connected = false;

            // Send leave message to server
            if (out != null) {
                Message leaveMsg = new Message(MessageType.LEAVE, username, "Disconnecting");
                out.println(leaveMsg.toProtocol());
            }

            // Stop listener thread
            if (listener != null) {
                listener.stopListening();
            }

            // Close streams
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

            // Close socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Disconnected from server");

        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Check if connected to server
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Get current username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Interface for handling received messages
     * UI classes will implement this to receive messages
     */
    public interface MessageHandler {
        void onMessageReceived(Message message);
    }
}
