package common;

/**
 * Constants used throughout the Enhanced Chat Application
 * Defines network settings, file transfer parameters, and system limits
 */
public class Constants {

    // ============ SERVER CONFIGURATION ============
    public static final int SERVER_PORT = 5000;
    public static final String SERVER_HOST = "localhost";
    public static final String SERVER_IP = "127.0.0.1";
    public static final int MAX_CLIENTS = 50;
    public static final int SERVER_BACKLOG = 10;
    public static final int FILE_TRANSFER_PORT = 5001;

    // ============ FILE TRANSFER CONFIGURATION ============
    public static final int FILE_BUFFER_SIZE = 8192; // 8KB chunks for file transfer
    public static final int BUFFER_SIZE = 8192; // General buffer size
    public static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB max file size
    public static final String FILE_TRANSFER_DIRECTORY = "received_files/";
    public static final String FILE_TRANSFER_DIR = "transferred_files/";
    public static final String FILES_DIR = "uploaded_files";
    public static final int FILE_TRANSFER_TIMEOUT = 30000; // 30 seconds

    // ============ MESSAGE PROTOCOL ============
    public static final String MESSAGE_DELIMITER = "|||";
    public static final String FILE_METADATA_DELIMITER = "::";
    public static final int MAX_MESSAGE_LENGTH = 1000;

    // ============ SOCKET TIMEOUTS ============
    public static final int SOCKET_TIMEOUT = 60000; // 60 seconds
    public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds

    // ============ CHAT CONFIGURATION ============
    public static final String CHAT_HISTORY_FILE = "logs/chat_history.txt";
    public static final int MAX_CHAT_HISTORY_LINES = 10000;
    public static final int MAX_HISTORY_SIZE = 100;

    // ============ UI CONFIGURATION ============
    public static final int UI_WIDTH = 800;
    public static final int UI_HEIGHT = 600;
    public static final String APP_TITLE = "Enhanced Chat Application";
    public static final String ADMIN_TITLE = "Admin Console";

    // ============ USER MANAGEMENT ============
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final String SERVER_NAME = "SERVER";
    public static final String WELCOME_MESSAGE = "Welcome to Enhanced Chat System!";
    public static final String DISCONNECT_MESSAGE = "has left the chat";
    public static final String CONNECT_MESSAGE = "has joined the chat";

    // ============ ADMIN CREDENTIALS ============
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    // ============ COMMANDS ============
    public static final String EXIT_COMMAND = "/exit";
    public static final String PRIVATE_COMMAND = "/private";
    public static final String FILE_COMMAND = "/file";
    public static final String HELP_COMMAND = "/help";

    // ============ FILE TRANSFER STATUS ============
    public static final String FILE_STATUS_PENDING = "PENDING";
    public static final String FILE_STATUS_ACCEPTED = "ACCEPTED";
    public static final String FILE_STATUS_REJECTED = "REJECTED";
    public static final String FILE_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String FILE_STATUS_COMPLETED = "COMPLETED";
    public static final String FILE_STATUS_FAILED = "FAILED";

    // ============ OPTIONAL PORTS ============
    // WebSocket port for browser clients (if needed in future)
    public static final int WS_PORT = 8080;
    // HTTP port for file uploads/downloads (if needed in future)
    public static final int FILE_HTTP_PORT = 9000;

    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Cannot instantiate Constants class");
    }
}
