package server;

import common.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles chat logging functionality
 */
public class ChatLogger {
    private static final String LOG_DIR = "logs";
    private static final String CHAT_LOG_FILE = "chat_log.txt";
    private static final String SERVER_LOG_FILE = "server_log.txt";
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private PrintWriter chatLogWriter;
    private PrintWriter serverLogWriter;
    
    public ChatLogger() {
        initializeLogFiles();
    }
    
    /**
     * Initialize log files and directories
     */
    private void initializeLogFiles() {
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Initialize chat log writer
            chatLogWriter = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(LOG_DIR + File.separator + CHAT_LOG_FILE, true)
                ), true
            );
            
            // Initialize server log writer
            serverLogWriter = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(LOG_DIR + File.separator + SERVER_LOG_FILE, true)
                ), true
            );
            
            // Log session start
            String sessionStart = String.format("[%s] ========== Server Session Started ==========",
                LocalDateTime.now().format(formatter));
            chatLogWriter.println(sessionStart);
            serverLogWriter.println(sessionStart);
            
        } catch (IOException e) {
            System.err.println("Error initializing log files: " + e.getMessage());
        }
    }
    
    /**
     * Log a chat message
     */
    public void logChatMessage(Message message) {
        if (chatLogWriter == null) return;
        
        String logEntry = String.format("[%s] [%s] %s -> %s: %s",
            message.getTimestamp(),
            message.getType(),
            message.getSender(),
            message.getReceiver() != null ? message.getReceiver() : "ALL",
            message.getContent()
        );
        
        chatLogWriter.println(logEntry);
        chatLogWriter.flush();
    }
    
    /**
     * Log a server event
     */
    public void logServerEvent(String event) {
        if (serverLogWriter == null) return;
        
        String logEntry = String.format("[%s] %s",
            LocalDateTime.now().format(formatter),
            event
        );
        
        serverLogWriter.println(logEntry);
        serverLogWriter.flush();
    }
    
    /**
     * Log user connection
     */
    public void logUserConnected(String username) {
        logServerEvent("USER_CONNECTED: " + username);
    }
    
    /**
     * Log user disconnection
     */
    public void logUserDisconnected(String username) {
        logServerEvent("USER_DISCONNECTED: " + username);
    }
    
    /**
     * Log file transfer
     */
    public void logFileTransfer(String sender, String receiver, String filename, String status) {
        String event = String.format("FILE_TRANSFER [%s]: %s -> %s (File: %s)",
            status, sender, receiver, filename);
        logServerEvent(event);
    }
    
    /**
     * Log admin action
     */
    public void logAdminAction(String action) {
        logServerEvent("ADMIN_ACTION: " + action);
    }
    
    /**
     * Get recent chat logs (last n lines)
     */
    public List<String> getRecentChatLogs(int lines) {
        return getRecentLogs(LOG_DIR + File.separator + CHAT_LOG_FILE, lines);
    }
    
    /**
     * Get recent server logs (last n lines)
     */
    public List<String> getRecentServerLogs(int lines) {
        return getRecentLogs(LOG_DIR + File.separator + SERVER_LOG_FILE, lines);
    }
    
    /**
     * Get recent logs from a file
     */
    private List<String> getRecentLogs(String filename, int lines) {
        List<String> allLines = new ArrayList<>();
        
        try {
            allLines = Files.readAllLines(Paths.get(filename));
            
            // Return last n lines
            int startIndex = Math.max(0, allLines.size() - lines);
            return allLines.subList(startIndex, allLines.size());
            
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Search logs for a specific term
     */
    public List<String> searchLogs(String searchTerm, boolean chatLogs) {
        String filename = chatLogs ? 
            LOG_DIR + File.separator + CHAT_LOG_FILE : 
            LOG_DIR + File.separator + SERVER_LOG_FILE;
        
        try {
            return Files.readAllLines(Paths.get(filename))
                .stream()
                .filter(line -> line.toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error searching logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Clear chat logs
     */
    public void clearChatLogs() {
        clearLogFile(LOG_DIR + File.separator + CHAT_LOG_FILE);
        logServerEvent("Chat logs cleared");
    }
    
    /**
     * Clear server logs
     */
    public void clearServerLogs() {
        clearLogFile(LOG_DIR + File.separator + SERVER_LOG_FILE);
    }
    
    /**
     * Clear a log file
     */
    private void clearLogFile(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename);
            writer.print("");
            writer.close();
            
            String sessionStart = String.format("[%s] ========== Log Cleared ==========",
                LocalDateTime.now().format(formatter));
            
            // Reinitialize the file with header
            PrintWriter newWriter = new PrintWriter(
                new BufferedWriter(new FileWriter(filename, true)), true
            );
            newWriter.println(sessionStart);
            newWriter.close();
            
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }
    
    /**
     * Close log writers
     */
    public void close() {
        String sessionEnd = String.format("[%s] ========== Server Session Ended ==========",
            LocalDateTime.now().format(formatter));
        
        if (chatLogWriter != null) {
            chatLogWriter.println(sessionEnd);
            chatLogWriter.close();
        }
        
        if (serverLogWriter != null) {
            serverLogWriter.println(sessionEnd);
            serverLogWriter.close();
        }
    }
}
