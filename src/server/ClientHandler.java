package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean isAdmin = false;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            // Set up streams
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // First message should be login or admin login
            Message loginMessage = (Message) input.readObject();
            
            if (MessageType.ADMIN_LOGIN.equals(loginMessage.getType())) {
                handleAdminLogin(loginMessage);
            } else if (MessageType.LOGIN.equals(loginMessage.getType())) {
                handleClientLogin(loginMessage);
            } else {
                sendMessage(new Message(MessageType.ERROR, "SERVER", "Invalid login message"));
                disconnect();
                return;
            }
            
            // Main message loop
            if (isAdmin) {
                handleAdminCommands();
            } else {
                handleClientMessages();
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    // Handle admin login
    private void handleAdminLogin(Message loginMessage) throws IOException {
        String[] credentials = loginMessage.getContent().split(":");
        
        if (credentials.length == 2 && 
            Constants.ADMIN_USERNAME.equals(credentials[0]) && 
            Constants.ADMIN_PASSWORD.equals(credentials[1])) {
            
            isAdmin = true;
            username = "ADMIN";
            Server.addAdminSocket(socket);
            
            sendMessage(new Message(MessageType.ADMIN_AUTH_SUCCESS, "SERVER", "Admin authentication successful"));
            
            // Send initial data
            sendConnectedClientsList();
            sendChatHistory();
            sendSystemStats();
            
        } else {
            sendMessage(new Message(MessageType.ADMIN_AUTH_FAILED, "SERVER", "Invalid admin credentials"));
            disconnect();
        }
    }
    
    // Handle client login
    private void handleClientLogin(Message loginMessage) throws IOException {
        username = loginMessage.getContent();
        
        // Check if username is already taken
        if (Server.getConnectedClients().stream().anyMatch(c -> c.contains(username))) {
            sendMessage(new Message(MessageType.ERROR, "SERVER", "Username already taken"));
            disconnect();
            return;
        }
        
        Server.addClient(username, this);
        sendMessage(new Message(MessageType.LOGIN, "SERVER", "Login successful"));
    }
    
    // Handle admin commands
    private void handleAdminCommands() throws IOException, ClassNotFoundException {
        while (true) {
            Message message = (Message) input.readObject();
            String messageType = message.getType();
            
            switch (messageType) {
                case MessageType.GET_CONNECTED_CLIENTS:
                    sendConnectedClientsList();
                    break;
                    
                case MessageType.GET_CHAT_HISTORY:
                    sendChatHistory();
                    break;
                    
                case MessageType.GET_SYSTEM_STATS:
                    sendSystemStats();
                    break;
                    
                case MessageType.KICK_USER:
                    handleKickUser(message.getContent());
                    break;
                    
                case MessageType.LOGOUT:
                    return;
                    
                default:
                    sendMessage(new Message(MessageType.ERROR, "SERVER", "Unknown command"));
            }
        }
    }
    
    // Handle client messages
    private void handleClientMessages() throws IOException, ClassNotFoundException {
        while (true) {
            Message message = (Message) input.readObject();
            String messageType = message.getType();
            
            switch (messageType) {
                case MessageType.PUBLIC_MESSAGE:
                    Server.broadcastMessage(new Message(MessageType.MESSAGE_BROADCAST, username, message.getContent()));
                    break;
                    
                case MessageType.PRIVATE_MESSAGE:
                    Server.sendPrivateMessage(username, message.getReceiver(), message.getContent());
                    break;
                    
                case MessageType.FILE_TRANSFER:
                    Server.incrementFileTransfers();
                    break;
                    
                case MessageType.LOGOUT:
                    return;
                    
                default:
                    sendMessage(new Message(MessageType.ERROR, "SERVER", "Unknown message type"));
            }
        }
    }
    
    // Send connected clients list
    private void sendConnectedClientsList() throws IOException {
        String clientList = String.join("\n", Server.getConnectedClients());
        if (clientList.isEmpty()) {
            clientList = "No clients connected";
        }
        sendMessage(new Message(MessageType.CONNECTED_CLIENTS_LIST, "SERVER", clientList));
    }
    
    // Send chat history
    private void sendChatHistory() throws IOException {
        StringBuilder history = new StringBuilder();
        for (Message msg : Server.getChatHistory()) {
            history.append(msg.toString()).append("\n");
        }
        
        if (history.length() == 0) {
            history.append("No chat history available");
        }
        
        sendMessage(new Message(MessageType.CHAT_HISTORY_DATA, "SERVER", history.toString()));
    }
    
    // Send system statistics
    private void sendSystemStats() throws IOException {
        String stats = Server.getSystemStats();
        sendMessage(new Message(MessageType.SYSTEM_STATS_DATA, "SERVER", stats));
    }
    
    // Handle kick user command
    private void handleKickUser(String targetUsername) throws IOException {
        if (Server.kickUser(targetUsername)) {
            sendMessage(new Message(MessageType.KICK_SUCCESS, "SERVER", "User " + targetUsername + " has been kicked"));
        } else {
            sendMessage(new Message(MessageType.KICK_FAILED, "SERVER", "User " + targetUsername + " not found"));
        }
    }
    
    // Send message to this client
    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + username + ": " + e.getMessage());
        }
    }
    
    // Get client address
    public String getClientAddress() {
        return socket.getInetAddress().getHostAddress();
    }
    
    // Disconnect client
    public void disconnect() {
        try {
            if (isAdmin) {
                Server.removeAdminSocket(socket);
            } else if (username != null) {
                Server.removeClient(username);
            }
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
        } catch (IOException e) {
            System.err.println("Error disconnecting client: " + e.getMessage());
        }
    }
}
