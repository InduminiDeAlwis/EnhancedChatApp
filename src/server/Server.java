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
        
        // Start Web Admin Server
        WebAdminServer webAdmin = new WebAdminServer(8080);
        webAdmin.start();
        
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
            return true;
        }
        return false;
    }
    
    // Update all admin consoles with latest info
    private static void updateAdminConsoles() {
        // This will be called by admin handlers when they request updates
    }
    
    // Shutdown server
    private static void shutdown() {
        try {
            System.out.println("\nShutting down server...");
            
            // Disconnect all clients
            for (ClientHandler client : clients.values()) {
                client.disconnect();
            }
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            System.out.println("Server shutdown complete");
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
}
