package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin console for server management
 */
public class AdminConsole implements Runnable {
    private final Server server;
    private final BufferedReader reader;
    private boolean running;
    private final LocalDateTime startTime;
    
    public AdminConsole(Server server) {
        this.server = server;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.running = true;
        this.startTime = LocalDateTime.now();
    }
    
    @Override
    public void run() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║           Admin Console Started                      ║");
        System.out.println("║   Type 'help' for available commands                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
        
        while (running) {
            try {
                System.out.print("admin> ");
                String command = reader.readLine();
                
                if (command != null && !command.trim().isEmpty()) {
                    processCommand(command.trim());
                }
                
            } catch (IOException e) {
                System.err.println("Error reading command: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process admin commands
     */
    private void processCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "help":
                displayHelp();
                break;
                
            case "list":
            case "users":
                listUsers();
                break;
                
            case "stats":
            case "status":
                showStatistics();
                break;
                
            case "announce":
            case "broadcast":
                if (parts.length > 1) {
                    sendAnnouncement(parts[1]);
                } else {
                    System.out.println("Usage: announce <message>");
                }
                break;
                
            case "kick":
                if (parts.length > 1) {
                    kickUser(parts[1]);
                } else {
                    System.out.println("Usage: kick <username>");
                }
                break;
                
            case "shutdown":
            case "stop":
            case "exit":
                shutdownServer();
                break;
                
            case "clear":
            case "cls":
                clearScreen();
                break;
                
            case "logs":
            case "chatlogs":
                if (parts.length > 1) {
                    try {
                        int lines = Integer.parseInt(parts[1]);
                        viewChatLogs(lines);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Usage: logs <number>");
                    }
                } else {
                    viewChatLogs(20); // Default 20 lines
                }
                break;
                
            case "serverlogs":
                if (parts.length > 1) {
                    try {
                        int lines = Integer.parseInt(parts[1]);
                        viewServerLogs(lines);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Usage: serverlogs <number>");
                    }
                } else {
                    viewServerLogs(20); // Default 20 lines
                }
                break;
                
            case "search":
                if (parts.length > 1) {
                    searchLogs(parts[1]);
                } else {
                    System.out.println("Usage: search <term>");
                }
                break;
                
            default:
                System.out.println("Unknown command: " + cmd);
                System.out.println("Type 'help' for available commands");
                break;
        }
    }
    
    /**
     * Display help menu
     */
    private void displayHelp() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              Admin Console Commands                  ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ help              - Show this help menu              ║");
        System.out.println("║ list/users        - List all online users            ║");
        System.out.println("║ stats/status      - Show server statistics           ║");
        System.out.println("║ announce <msg>    - Send server announcement         ║");
        System.out.println("║ kick <username>   - Kick a user from the server      ║");
        System.out.println("║ logs [n]          - View recent chat logs (def: 20)  ║");
        System.out.println("║ serverlogs [n]    - View server logs (default: 20)   ║");
        System.out.println("║ search <term>     - Search in chat logs              ║");
        System.out.println("║ clear/cls         - Clear the screen                 ║");
        System.out.println("║ shutdown/exit     - Shutdown the server              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * List all online users
     */
    private void listUsers() {
        List<String> users = server.getOnlineUsers();
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              Online Users (" + users.size() + "/" + 
            Constants.MAX_CLIENTS + ")                    ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        
        if (users.isEmpty()) {
            System.out.println("║ No users currently online                            ║");
        } else {
            for (int i = 0; i < users.size(); i++) {
                String line = String.format("║ %2d. %-46s ║", i + 1, users.get(i));
                System.out.println(line);
            }
        }
        
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Show server statistics
     */
    private void showStatistics() {
        long uptimeSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
        String uptime = formatUptime(uptimeSeconds);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              Server Statistics                       ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Server Port       : " + String.format("%-32s", Constants.SERVER_PORT) + " ║");
        System.out.println("║ Online Users      : " + String.format("%-32s", server.getClientCount()) + " ║");
        System.out.println("║ Max Capacity      : " + String.format("%-32s", Constants.MAX_CLIENTS) + " ║");
        System.out.println("║ Server Uptime     : " + String.format("%-32s", uptime) + " ║");
        System.out.println("║ Start Time        : " + String.format("%-32s", 
            startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) + " ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Format uptime in readable format
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
    
    /**
     * Send server announcement to all users
     */
    private void sendAnnouncement(String announcement) {
        Message msg = new Message(MessageType.SERVER_ANNOUNCEMENT, 
            Constants.SERVER_NAME, announcement);
        server.broadcastMessage(msg);
        
        System.out.println("✓ Announcement sent to all users: " + announcement);
    }
    
    /**
     * Kick a user from the server
     */
    private void kickUser(String username) {
        ClientHandler handler = server.getClientHandler(username);
        
        if (handler == null) {
            System.out.println("✗ User '" + username + "' not found");
            return;
        }
        
        // Send kick notification to user
        Message kickMsg = new Message(MessageType.SERVER_ANNOUNCEMENT, 
            Constants.SERVER_NAME, "You have been kicked from the server by admin");
        handler.sendMessage(kickMsg);
        
        // Disconnect the user
        handler.disconnect();
        
        System.out.println("✓ User '" + username + "' has been kicked from the server");
    }
    
    /**
     * Clear the console screen
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clearing fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Shutdown the server
     */
    private void shutdownServer() {
        System.out.print("Are you sure you want to shutdown the server? (yes/no): ");
        try {
            String confirmation = reader.readLine();
            if (confirmation != null && confirmation.equalsIgnoreCase("yes")) {
                running = false;
                server.shutdown();
                System.exit(0);
            } else {
                System.out.println("Shutdown cancelled");
            }
        } catch (IOException e) {
            System.err.println("Error reading confirmation: " + e.getMessage());
        }
    }
    
    /**
     * View recent chat logs
     */
    private void viewChatLogs(int lines) {
        ChatLogger logger = server.getChatLogger();
        List<String> logs = logger.getRecentChatLogs(lines);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         Recent Chat Logs (Last " + lines + " lines)          ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        
        if (logs.isEmpty()) {
            System.out.println("║ No chat logs available                               ║");
        } else {
            for (String log : logs) {
                // Truncate long lines to fit in the box
                if (log.length() > 52) {
                    log = log.substring(0, 49) + "...";
                }
                System.out.println(log);
            }
        }
        
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * View recent server logs
     */
    private void viewServerLogs(int lines) {
        ChatLogger logger = server.getChatLogger();
        List<String> logs = logger.getRecentServerLogs(lines);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║       Recent Server Logs (Last " + lines + " lines)        ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        
        if (logs.isEmpty()) {
            System.out.println("║ No server logs available                             ║");
        } else {
            for (String log : logs) {
                // Truncate long lines to fit in the box
                if (log.length() > 52) {
                    log = log.substring(0, 49) + "...";
                }
                System.out.println(log);
            }
        }
        
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Search logs for a specific term
     */
    private void searchLogs(String searchTerm) {
        ChatLogger logger = server.getChatLogger();
        List<String> results = logger.searchLogs(searchTerm, true);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         Search Results for: " + 
            String.format("%-25s", searchTerm.substring(0, Math.min(25, searchTerm.length()))) + " ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        
        if (results.isEmpty()) {
            System.out.println("║ No results found                                     ║");
        } else {
            System.out.println("║ Found " + results.size() + " matching entries:                        ║");
            System.out.println("╠══════════════════════════════════════════════════════╣");
            
            int count = 0;
            for (String result : results) {
                if (count >= 10) {
                    System.out.println("║ ... (" + (results.size() - 10) + " more results)                             ║");
                    break;
                }
                // Truncate long lines to fit in the box
                if (result.length() > 52) {
                    result = result.substring(0, 49) + "...";
                }
                System.out.println(result);
                count++;
            }
        }
        
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
}
