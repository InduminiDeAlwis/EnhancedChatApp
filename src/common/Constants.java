package common;

public class Constants {
    // Server Configuration
    public static final int SERVER_PORT = 5000;
    public static final int FILE_TRANSFER_PORT = 5001;
    public static final String SERVER_HOST = "localhost";
    
    // Buffer Sizes
    public static final int BUFFER_SIZE = 8192;
    public static final int FILE_BUFFER_SIZE = 4096;
    
    // Timeouts
    public static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    
    // Limits
    public static final int MAX_CLIENTS = 50;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB
    
    // Server Messages
    public static final String SERVER_NAME = "SERVER";
    public static final String WELCOME_MESSAGE = "Welcome to Enhanced Chat System!";
    public static final String DISCONNECT_MESSAGE = "has left the chat";
    public static final String CONNECT_MESSAGE = "has joined the chat";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
