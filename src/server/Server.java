package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main server class for the Enhanced Multi-Client Chat and File Transfer System
 */
public class Server {
    private static Server instance;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clientHandlers;
    private final List<String> onlineUsers;
    private boolean running;
    private int clientCounter;
    
    public Server() {
        this.clientHandlers = new ConcurrentHashMap<>();
        this.onlineUsers = new ArrayList<>();
        this.running = false;
        this.clientCounter = 0;
    }
    
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }
    
    /**
     * Start the server and begin accepting client connections
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            running = true;
            
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║   Enhanced Chat Server Started Successfully          ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.println("Server listening on port: " + Constants.SERVER_PORT);
            System.out.println("Waiting for client connections...\n");
            
            // Start admin console in separate thread
            Thread adminThread = new Thread(new AdminConsole(this));
            adminThread.setDaemon(true);
            adminThread.start();
            
            // Accept client connections
            acceptClients();
            
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Accept incoming client connections
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientCounter++;
                
                if (clientHandlers.size() >= Constants.MAX_CLIENTS) {
                    System.out.println("Server full. Rejecting connection from: " + 
                        clientSocket.getInetAddress().getHostAddress());
                    clientSocket.close();
                    continue;
                }
                
                System.out.println("New connection from: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                // Create and start client handler thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Register a client with the server
     */
    public synchronized boolean registerClient(String username, ClientHandler handler) {
        if (clientHandlers.containsKey(username)) {
            return false; // Username already taken
        }
        
        clientHandlers.put(username, handler);
        onlineUsers.add(username);
        
        System.out.println("✓ User '" + username + "' registered successfully. Total users: " + 
            clientHandlers.size());
        
        // Notify all clients about the new user
        broadcastMessage(new Message(MessageType.USER_JOINED, Constants.SERVER_NAME, 
            username + " " + Constants.CONNECT_MESSAGE));
        
        // Send updated user list to all clients
        broadcastUserList();
        
        return true;
    }
    
    /**
     * Remove a client from the server
     */
    public synchronized void removeClient(String username) {
        if (username == null || !clientHandlers.containsKey(username)) {
            return;
        }
        
        clientHandlers.remove(username);
        onlineUsers.remove(username);
        
        System.out.println("✗ User '" + username + "' disconnected. Total users: " + 
            clientHandlers.size());
        
        // Notify all clients about the user leaving
        broadcastMessage(new Message(MessageType.USER_LEFT, Constants.SERVER_NAME, 
            username + " " + Constants.DISCONNECT_MESSAGE));
        
        // Send updated user list to all clients
        broadcastUserList();
    }
    
    /**
     * Broadcast a message to all connected clients
     */
    public void broadcastMessage(Message message) {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(message);
        }
    }
    
    /**
     * Send a message to a specific client
     */
    public boolean sendPrivateMessage(Message message) {
        String receiver = message.getReceiver();
        ClientHandler handler = clientHandlers.get(receiver);
        
        if (handler != null) {
            handler.sendMessage(message);
            return true;
        }
        return false;
    }
    
    /**
     * Broadcast the current user list to all clients
     */
    public void broadcastUserList() {
        Message userListMessage = new Message(MessageType.USER_LIST, 
            Constants.SERVER_NAME, String.join(",", onlineUsers));
        broadcastMessage(userListMessage);
    }
    
    /**
     * Get the list of online users
     */
    public synchronized List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }
    
    /**
     * Get a specific client handler
     */
    public ClientHandler getClientHandler(String username) {
        return clientHandlers.get(username);
    }
    
    /**
     * Get the number of connected clients
     */
    public int getClientCount() {
        return clientHandlers.size();
    }
    
    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !clientHandlers.containsKey(username);
    }
    
    /**
     * Shutdown the server gracefully
     */
    public void shutdown() {
        System.out.println("\nShutting down server...");
        running = false;
        
        // Notify all clients
        broadcastMessage(new Message(MessageType.SERVER_ANNOUNCEMENT, 
            Constants.SERVER_NAME, "Server is shutting down. Goodbye!"));
        
        // Close all client connections
        for (ClientHandler handler : clientHandlers.values()) {
            handler.disconnect();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Server shutdown complete.");
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        Server server = Server.getInstance();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));
        
        server.startServer();
    }
}
