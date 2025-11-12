package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server.java
 * Main server class that handles multiple client connections
 * Uses multi-threading to handle each client simultaneously
 */
public class Server {

    private ServerSocket serverSocket;
    private boolean running;

    // Thread-safe collections to manage clients
    // ConcurrentHashMap allows multiple threads to access safely
    private ConcurrentHashMap<String, ClientHandler> clients;  // username -> ClientHandler
    private List<ClientHandler> clientHandlers;                // List of all handlers

    /**
     * Constructor - Initialize the server
     */
    public Server() {
        this.clients = new ConcurrentHashMap<>();
        this.clientHandlers = new ArrayList<>();
        this.running = false;
    }

    /**
     * Start the server and listen for connections
     */
    public void start() {
        try {
            // Create server socket on specified port
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            running = true;

            System.out.println("TPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPW");
            System.out.println("Q   Enhanced Chat Server Started Successfully  Q");
            System.out.println("ZPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP]");
            System.out.println("Server listening on port: " + Constants.SERVER_PORT);
            System.out.println("Maximum clients: " + Constants.MAX_CLIENTS);
            System.out.println("Waiting for client connections...\n");

            // Main server loop - accept client connections
            while (running) {
                try {
                    // Wait for a client to connect (blocking call)
                    Socket clientSocket = serverSocket.accept();

                    // Check if server is full
                    if (clientHandlers.size() >= Constants.MAX_CLIENTS) {
                        System.out.println("Server full. Rejecting connection from: " +
                                         clientSocket.getInetAddress().getHostAddress());

                        // Send rejection message and close
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println(new Message(MessageType.ERROR, Constants.SYSTEM_SENDER,
                                              "Server is full. Try again later.").toProtocol());
                        clientSocket.close();
                        continue;
                    }

                    // Create a new thread to handle this client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientHandlers.add(clientHandler);

                    // Start the client handler thread
                    new Thread(clientHandler).start();

                    System.out.println("New connection from: " +
                                     clientSocket.getInetAddress().getHostAddress());
                    System.out.println("Active connections: " + clientHandlers.size() + "\n");

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    /**
     * Stop the server and disconnect all clients
     */
    public void stop() {
        try {
            running = false;

            System.out.println("\nShutting down server...");

            // Disconnect all clients
            for (ClientHandler handler : clientHandlers) {
                handler.disconnect();
            }

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            System.out.println("Server stopped successfully.");

        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Register a client with their username
     * Called by ClientHandler after successful login
     */
    public synchronized boolean registerClient(String username, ClientHandler handler) {
        // Check if username is already taken
        if (clients.containsKey(username)) {
            return false;
        }

        // Register the client
        clients.put(username, handler);
        System.out.println(" User registered: " + username + " (Total users: " + clients.size() + ")");

        // Notify all clients that new user joined
        broadcastMessage(Message.systemMessage(username + " joined the chat"));

        // Send welcome message to the new user
        handler.sendMessage(Message.systemMessage(Constants.WELCOME_MESSAGE));

        // Send list of online users to the new client
        sendUserList(handler);

        return true;
    }

    /**
     * Unregister a client (when they disconnect)
     */
    public synchronized void unregisterClient(String username, ClientHandler handler) {
        clients.remove(username);
        clientHandlers.remove(handler);

        System.out.println(" User disconnected: " + username + " (Total users: " + clients.size() + ")");

        // Notify all clients that user left
        if (username != null) {
            broadcastMessage(Message.systemMessage(username + " left the chat"));
        }
    }

    /**
     * Broadcast message to all connected clients
     * Used for group chat messages and system notifications
     */
    public void broadcastMessage(Message message) {
        System.out.println("Broadcasting: " + message.toDisplayFormat());

        // Log the message
        logMessage(message);

        // Send to all clients
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }

    /**
     * Send private message to specific user
     * This is the core of your private chat feature!
     */
    public void sendPrivateMessage(Message message) {
        String recipient = message.getRecipient();
        String sender = message.getSender();

        System.out.println("Private message: " + sender + " -> " + recipient);

        // Log the private message
        logMessage(message);

        // Find recipient's handler
        ClientHandler recipientHandler = clients.get(recipient);

        if (recipientHandler != null) {
            // Send to recipient
            recipientHandler.sendMessage(message);

            // Also send confirmation to sender (so they see their own message)
            ClientHandler senderHandler = clients.get(sender);
            if (senderHandler != null) {
                senderHandler.sendMessage(message);
            }
        } else {
            // Recipient not found - send error to sender
            ClientHandler senderHandler = clients.get(sender);
            if (senderHandler != null) {
                Message errorMsg = new Message(MessageType.ERROR,
                                              Constants.SYSTEM_SENDER,
                                              sender,
                                              "User '" + recipient + "' is not online.");
                senderHandler.sendMessage(errorMsg);
            }
        }
    }

    /**
     * Send list of online users to a specific client
     */
    public void sendUserList(ClientHandler handler) {
        StringBuilder userList = new StringBuilder("Online users: ");
        for (String username : clients.keySet()) {
            userList.append(username).append(", ");
        }

        // Remove trailing comma
        if (userList.length() > 14) {
            userList.setLength(userList.length() - 2);
        }

        Message userListMsg = new Message(MessageType.USER_LIST,
                                         Constants.SYSTEM_SENDER,
                                         userList.toString());
        handler.sendMessage(userListMsg);
    }

    /**
     * Get list of all online usernames
     * Used by clients to show who's available for private chat
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }

    /**
     * Log message to file for chat history
     */
    private void logMessage(Message message) {
        if (!Constants.ENABLE_LOGGING) {
            return;
        }

        try {
            File logFile = new File(Constants.CHAT_HISTORY_FILE);

            // Create parent directories if they don't exist
            logFile.getParentFile().mkdirs();

            // Append to log file
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timestamp = LocalDateTime.now().format(formatter);

                out.println("[" + timestamp + "] " + message.toDisplayFormat());
            }
        } catch (IOException e) {
            System.err.println("Error logging message: " + e.getMessage());
        }
    }

    /**
     * Main method - Entry point for server application
     */
    public static void main(String[] args) {
        Server server = new Server();

        // Add shutdown hook to gracefully stop server on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown signal received...");
            server.stop();
        }));

        // Start the server
        server.start();
    }
}
