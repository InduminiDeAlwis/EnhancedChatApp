package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private static ServerSocket serverSocket;
    
    // Thread-safe collections
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static List<Message> chatHistory = new CopyOnWriteArrayList<>();
    private static Set<Socket> adminSockets = ConcurrentHashMap.newKeySet();
    
    // Statistics
    private static int totalMessagesSent = 0;
    private static int totalFilesTransferred = 0;
    private static LocalDateTime serverStartTime;
    
    public static void main(String[] args) {
        serverStartTime = LocalDateTime.now();
        System.out.println("=".repeat(50));
        System.out.println("Enhanced Chat Server Starting...");
        System.out.println("=".repeat(50));
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("✓ Server started on port " + PORT);
            System.out.println("✓ Waiting for client connections...");
            System.out.println("=".repeat(50));
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[NEW CONNECTION] " + clientSocket.getInetAddress().getHostAddress());
                
                // Start a new thread to handle this client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
            
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    // Add client to the map
    public static synchronized void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        System.out.println("[USER JOINED] " + username + " | Total clients: " + clients.size());
        
        // Notify all clients about new user
        broadcastMessage(new Message(MessageType.USER_JOINED, "SERVER", username + " has joined the chat"));
        
        // Update all admin consoles
        updateAdminConsoles();
    }
    
    // Remove client from the map
    public static synchronized void removeClient(String username) {
        if (clients.remove(username) != null) {
            System.out.println("[USER LEFT] " + username + " | Total clients: " + clients.size());
            
            // Notify all clients about user leaving
            broadcastMessage(new Message(MessageType.USER_LEFT, "SERVER", username + " has left the chat"));
            
            // Update all admin consoles
            updateAdminConsoles();
        }
    }
    
    // Add admin socket
    public static synchronized void addAdminSocket(Socket socket) {
        adminSockets.add(socket);
        System.out.println("[ADMIN CONNECTED] " + socket.getInetAddress().getHostAddress());
    }
    
    // Remove admin socket
    public static synchronized void removeAdminSocket(Socket socket) {
        adminSockets.remove(socket);
        System.out.println("[ADMIN DISCONNECTED] " + socket.getInetAddress().getHostAddress());
    }
    
    // Broadcast message to all clients
    public static void broadcastMessage(Message message) {
        // Add to history
        addToChatHistory(message);
        
        // Increment message counter
        totalMessagesSent++;
        
        // Send to all clients
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    // Send private message
    public static void sendPrivateMessage(String sender, String receiver, String content) {
        Message message = new Message(MessageType.PRIVATE_MESSAGE_RECEIVED, sender, receiver, content);
        addToChatHistory(message);
        totalMessagesSent++;
        
        ClientHandler receiverHandler = clients.get(receiver);
        if (receiverHandler != null) {
            receiverHandler.sendMessage(message);
            
            // Also send to sender for confirmation
            ClientHandler senderHandler = clients.get(sender);
            if (senderHandler != null) {
                senderHandler.sendMessage(message);
            }
        }
    }
    
    // Add message to chat history
    private static void addToChatHistory(Message message) {
        chatHistory.add(message);
        
        // Keep only last MAX_HISTORY_SIZE messages
        if (chatHistory.size() > Constants.MAX_HISTORY_SIZE) {
            chatHistory.remove(0);
        }
    }
    
    // Get connected clients list
    public static List<String> getConnectedClients() {
        List<String> clientList = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            String clientInfo = String.format("%d. %s (%s)", 
                index++, 
                entry.getKey(), 
                entry.getValue().getClientAddress());
            clientList.add(clientInfo);
        }
        return clientList;
    }
    
    // Get chat history
    public static List<Message> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }
    
    // Get system statistics
    public static String getSystemStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== System Statistics ===\n");
        stats.append("Server Start Time: ").append(serverStartTime).append("\n");
        stats.append("Active Users: ").append(clients.size()).append("\n");
        stats.append("Total Messages Sent: ").append(totalMessagesSent).append("\n");
        stats.append("Files Transferred: ").append(totalFilesTransferred).append("\n");
        stats.append("Chat History Size: ").append(chatHistory.size()).append("\n");
        stats.append("Admin Consoles Connected: ").append(adminSockets.size()).append("\n");
        stats.append("========================");
        return stats.toString();
    }
    
    // Increment file transfer counter
    public static void incrementFileTransfers() {
        totalFilesTransferred++;
    }
    
    // Kick user
    public static boolean kickUser(String username) {
        ClientHandler handler = clients.get(username);
        if (handler != null) {
            handler.disconnect();
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
    @SuppressWarnings("unused")
    private int clientCounter;
    private ChatLogger chatLogger;

    public Server() {
        this.clientHandlers = new ConcurrentHashMap<>();
        this.onlineUsers = new ArrayList<>();
        this.running = false;
        this.clientCounter = 0;
        this.chatLogger = new ChatLogger();
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

        // Log user connection
        chatLogger.logUserConnected(username);

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

        // Log user disconnection
        chatLogger.logUserDisconnected(username);

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
        // Log the message if it's a chat message
        if (message.getType() == MessageType.PUBLIC_MESSAGE) {
            chatLogger.logChatMessage(message);
        }

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
            // Log private message
            if (message.getType() == MessageType.PRIVATE_MESSAGE) {
                chatLogger.logChatMessage(message);
            }
            handler.sendMessage(message);
            return true;
        }
        return false;
    }
    
    // Update all admin consoles with latest client list
    private static void updateAdminConsoles() {
        List<String> clientList = getConnectedClients();
        String clientListData = String.join("\n", clientList);
        
        // Send update to all connected admin consoles
        for (Socket adminSocket : adminSockets) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(adminSocket.getOutputStream());
                Message updateMsg = new Message(MessageType.CONNECTED_CLIENTS_LIST, "SERVER", clientListData);
                out.writeObject(updateMsg);
                out.flush();
            } catch (IOException e) {
                // Admin socket might be closed, remove it
                adminSockets.remove(adminSocket);
            }
        }
    }
    
    // Shutdown server gracefully
    private static void shutdown() {
        try {
            System.out.println("\n[SHUTDOWN] Closing server...");
            
            // Close all client connections
            for (ClientHandler client : clients.values()) {
                client.disconnect();
            }
            
            // Close admin connections
            for (Socket adminSocket : adminSockets) {
                adminSocket.close();
            }
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            System.out.println("[SHUTDOWN] Server closed successfully");
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
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
     * Get the chat logger instance
     */
    public ChatLogger getChatLogger() {
        return chatLogger;
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

        // Close chat logger
        if (chatLogger != null) {
            chatLogger.close();
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
