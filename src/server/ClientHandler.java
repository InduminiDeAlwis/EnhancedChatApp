package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler.java
 * Handles communication with a single client in a separate thread
 * Each connected client gets their own ClientHandler instance
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private Server server;
    private String username;

    // I/O streams for communication
    private BufferedReader in;
    private PrintWriter out;

    private boolean connected;

    /**
     * Constructor
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.connected = true;
    }

    /**
     * Main thread execution method
     * This runs in a separate thread for each client
     */
    @Override
    public void run() {
        try {
            // Set up I/O streams
            setupStreams();

            // Authenticate user (get username)
            if (!authenticate()) {
                disconnect();
                return;
            }

            // Main message receiving loop
            listenForMessages();

        } catch (IOException e) {
            System.err.println("Error in ClientHandler for " + username + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Set up input/output streams for socket communication
     */
    private void setupStreams() throws IOException {
        // Input stream - read messages from client
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Output stream - send messages to client
        out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Streams established for client: " + socket.getInetAddress());
    }

    /**
     * Authenticate user - get and validate username
     */
    private boolean authenticate() throws IOException {
        // Ask for username
        sendMessage(Message.systemMessage("Enter your username:"));

        // Read username from client
        String receivedUsername = in.readLine();

        if (receivedUsername == null || receivedUsername.trim().isEmpty()) {
            sendMessage(Message.systemMessage("Invalid username. Disconnecting."));
            return false;
        }

        receivedUsername = receivedUsername.trim();

        // Check if username is valid (alphanumeric, 3-20 chars)
        if (!isValidUsername(receivedUsername)) {
            sendMessage(new Message(MessageType.ERROR,
                                   Constants.SYSTEM_SENDER,
                                   "Username must be 3-20 alphanumeric characters."));
            return false;
        }

        // Try to register with server
        if (server.registerClient(receivedUsername, this)) {
            this.username = receivedUsername;
            System.out.println(" Client authenticated as: " + username);
            return true;
        } else {
            sendMessage(new Message(MessageType.ERROR,
                                   Constants.SYSTEM_SENDER,
                                   "Username '" + receivedUsername + "' is already taken."));
            return false;
        }
    }

    /**
     * Validate username format
     */
    private boolean isValidUsername(String username) {
        return username != null &&
               username.length() >= 3 &&
               username.length() <= 20 &&
               username.matches("[a-zA-Z0-9_]+");
    }

    /**
     * Listen for incoming messages from client
     * This runs continuously until client disconnects
     */
    private void listenForMessages() throws IOException {
        String receivedData;

        while (connected && (receivedData = in.readLine()) != null) {
            try {
                // Parse received message
                Message message = Message.fromProtocol(receivedData);

                if (message == null) {
                    System.err.println("Invalid message format from " + username);
                    continue;
                }

                // Set sender to this client's username (security measure)
                message.setSender(username);

                // Handle different message types
                handleMessage(message);

            } catch (Exception e) {
                System.err.println("Error processing message from " + username + ": " + e.getMessage());
            }
        }
    }

    /**
     * Handle different types of messages
     */
    private void handleMessage(Message message) {
        System.out.println("Received from " + username + ": " + message.getType() + " - " + message.getContent());

        switch (message.getType()) {
            case BROADCAST:
                // Regular group chat message - broadcast to everyone
                server.broadcastMessage(message);
                break;

            case PRIVATE:
                // Private message - send only to specific recipient
                // This is your feature!
                server.sendPrivateMessage(message);
                break;

            case USER_LIST:
                // Client requesting list of online users
                server.sendUserList(this);
                break;

            case LEAVE:
                // Client wants to disconnect
                disconnect();
                break;

            default:
                System.err.println("Unknown message type from " + username + ": " + message.getType());
                break;
        }
    }

    /**
     * Send message to this client
     */
    public void sendMessage(Message message) {
        if (out != null && connected) {
            out.println(message.toProtocol());
        }
    }

    /**
     * Disconnect this client
     */
    public void disconnect() {
        if (!connected) {
            return; // Already disconnected
        }

        connected = false;

        try {
            // Unregister from server
            if (username != null) {
                server.unregisterClient(username, this);
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

            System.out.println(" Connection closed for: " + username);

        } catch (IOException e) {
            System.err.println("Error disconnecting client " + username + ": " + e.getMessage());
        }
    }

    /**
     * Get client's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Check if client is still connected
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
