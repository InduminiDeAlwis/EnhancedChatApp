package common;

/**
 * Constants.java
 * Stores all configuration constants used across the application
 * This ensures consistency between server and client
 */
public class Constants {

    // Server Configuration
    public static final String SERVER_HOST = "localhost";  // Server address (use "localhost" for local testing)
    public static final int SERVER_PORT = 8888;           // Port number the server listens on

    // Connection Settings
    public static final int MAX_CLIENTS = 50;             // Maximum number of simultaneous clients
    public static final int SOCKET_TIMEOUT = 0;           // Socket timeout in ms (0 = no timeout)

    // Message Protocol Delimiters
    public static final String MESSAGE_DELIMITER = "|";   // Separator for message parts
    public static final String MESSAGE_END = "\n";        // End of message marker

    // Message Format: TYPE|SENDER|RECIPIENT|CONTENT
    public static final int MSG_TYPE_INDEX = 0;           // Index of message type in split array
    public static final int MSG_SENDER_INDEX = 1;         // Index of sender in split array
    public static final int MSG_RECIPIENT_INDEX = 2;      // Index of recipient in split array
    public static final int MSG_CONTENT_INDEX = 3;        // Index of content in split array

    // Commands
    public static final String CMD_EXIT = "/exit";        // Command to disconnect from server
    public static final String CMD_LIST_USERS = "/users"; // Command to list online users
    public static final String CMD_PRIVATE = "/private";  // Command prefix for private messages

    // System Messages
    public static final String SYSTEM_SENDER = "SERVER";  // Sender name for system messages
    public static final String WELCOME_MESSAGE = "Welcome to Enhanced Chat Application!";
    public static final String DISCONNECT_MESSAGE = "You have been disconnected from the server.";

    // File Transfer Settings
    public static final int FILE_BUFFER_SIZE = 4096;      // Buffer size for file transfer (4KB)
    public static final String FILE_STORAGE_PATH = "./received_files/"; // Path to store received files

    // UI Settings
    public static final int WINDOW_WIDTH = 600;           // Default window width
    public static final int WINDOW_HEIGHT = 500;          // Default window height
    public static final int CHAT_AREA_ROWS = 20;          // Rows in chat text area
    public static final int CHAT_AREA_COLS = 50;          // Columns in chat text area

    // Chat History
    public static final String CHAT_HISTORY_FILE = "src/logs/chat_history.txt";
    public static final boolean ENABLE_LOGGING = true;    // Enable/disable chat logging

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
