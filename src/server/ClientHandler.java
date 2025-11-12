package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single client
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean running;
    
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            // Initialize streams
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Authenticate user
            if (!authenticateUser()) {
                disconnect();
                return;
            }
            
            // Send welcome message
            sendMessage(new Message(MessageType.SUCCESS, Constants.SERVER_NAME, 
                Constants.WELCOME_MESSAGE));
            
            // Main message handling loop
            while (running) {
                try {
                    Message message = (Message) input.readObject();
                    if (message != null) {
                        handleMessage(message);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Invalid message format from " + username);
                }
            }
            
        } catch (SocketException e) {
            System.out.println("Connection lost with " + username);
        } catch (IOException e) {
            if (running) {
                System.err.println("Error in ClientHandler for " + username + ": " + 
                    e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Authenticate the user during login
     */
    private boolean authenticateUser() throws IOException {
        try {
            Message loginMessage = (Message) input.readObject();
            
            if (loginMessage.getType() != MessageType.LOGIN) {
                sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                    "Invalid login request"));
                return false;
            }
            
            String requestedUsername = loginMessage.getSender();
            
            // Validate username
            if (requestedUsername == null || requestedUsername.trim().isEmpty()) {
                sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                    "Username cannot be empty"));
                return false;
            }
            
            if (requestedUsername.length() > Constants.MAX_USERNAME_LENGTH) {
                sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                    "Username too long (max " + Constants.MAX_USERNAME_LENGTH + " characters)"));
                return false;
            }
            
            // Check if username is available
            if (!server.isUsernameAvailable(requestedUsername)) {
                sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                    "Username already taken"));
                return false;
            }
            
            // Register client with server
            this.username = requestedUsername;
            if (server.registerClient(username, this)) {
                sendMessage(new Message(MessageType.LOGIN_SUCCESS, Constants.SERVER_NAME, 
                    "Login successful. Welcome " + username + "!"));
                return true;
            } else {
                sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                    "Failed to register user"));
                return false;
            }
            
        } catch (ClassNotFoundException e) {
            sendMessage(new Message(MessageType.LOGIN_FAILED, Constants.SERVER_NAME, 
                "Invalid login message format"));
            return false;
        }
    }
    
    /**
     * Handle incoming messages from the client
     */
    private void handleMessage(Message message) {
        if (message == null) return;
        
        System.out.println("Message from " + username + ": " + message.getType());
        
        switch (message.getType()) {
            case PUBLIC_MESSAGE:
                handlePublicMessage(message);
                break;
                
            case PRIVATE_MESSAGE:
                handlePrivateMessage(message);
                break;
                
            case FILE_TRANSFER_REQUEST:
            case FILE_TRANSFER_ACCEPT:
            case FILE_TRANSFER_REJECT:
                handleFileTransfer(message);
                break;
                
            case LOGOUT:
                handleLogout();
                break;
                
            case PING:
                sendMessage(new Message(MessageType.PONG, Constants.SERVER_NAME, "pong"));
                break;
                
            default:
                System.out.println("Unhandled message type: " + message.getType());
                break;
        }
    }
    
    /**
     * Handle public messages
     */
    private void handlePublicMessage(Message message) {
        // Validate message
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            sendMessage(new Message(MessageType.ERROR, Constants.SERVER_NAME, 
                "Message cannot be empty"));
            return;
        }
        
        if (message.getContent().length() > Constants.MAX_MESSAGE_LENGTH) {
            sendMessage(new Message(MessageType.ERROR, Constants.SERVER_NAME, 
                "Message too long (max " + Constants.MAX_MESSAGE_LENGTH + " characters)"));
            return;
        }
        
        // Set sender to authenticated username
        message.setSender(username);
        
        // Broadcast to all clients
        server.broadcastMessage(message);
    }
    
    /**
     * Handle private messages
     */
    private void handlePrivateMessage(Message message) {
        String receiver = message.getReceiver();
        
        if (receiver == null || receiver.trim().isEmpty()) {
            sendMessage(new Message(MessageType.ERROR, Constants.SERVER_NAME, 
                "Receiver not specified"));
            return;
        }
        
        // Validate message content
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            sendMessage(new Message(MessageType.ERROR, Constants.SERVER_NAME, 
                "Message cannot be empty"));
            return;
        }
        
        // Set sender to authenticated username
        message.setSender(username);
        
        // Use PrivateChatHandler to route the message
        PrivateChatHandler privateChatHandler = new PrivateChatHandler(server);
        if (!privateChatHandler.routePrivateMessage(message)) {
            sendMessage(new Message(MessageType.ERROR, Constants.SERVER_NAME, 
                "Failed to send private message. User may be offline."));
        }
    }
    
    /**
     * Handle file transfer messages
     */
    private void handleFileTransfer(Message message) {
        PrivateChatHandler privateChatHandler = new PrivateChatHandler(server);
        privateChatHandler.handleFileTransfer(message);
    }
    
    /**
     * Handle logout request
     */
    private void handleLogout() {
        System.out.println("User " + username + " requested logout");
        disconnect();
    }
    
    /**
     * Send a message to this client
     */
    public synchronized void sendMessage(Message message) {
        try {
            if (output != null && socket.isConnected() && !socket.isClosed()) {
                output.writeObject(message);
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + username + ": " + 
                e.getMessage());
            disconnect();
        }
    }
    
    /**
     * Disconnect and cleanup
     */
    public void disconnect() {
        if (!running) return;
        
        running = false;
        
        // Remove from server
        if (username != null) {
            server.removeClient(username);
        }
        
        // Close streams and socket
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        System.out.println("ClientHandler for " + username + " terminated");
    }
    
    /**
     * Get the username of this client
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Check if the handler is still running
     */
    public boolean isRunning() {
        return running;
    }
}
