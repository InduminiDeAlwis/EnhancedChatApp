package common;

public class MessageType {
    // Client-to-Server message types
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String PUBLIC_MESSAGE = "PUBLIC_MESSAGE";
    public static final String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    public static final String FILE_TRANSFER = "FILE_TRANSFER";
    
    // Admin-specific message types
    public static final String ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String GET_CONNECTED_CLIENTS = "GET_CONNECTED_CLIENTS";
    public static final String GET_CHAT_HISTORY = "GET_CHAT_HISTORY";
    public static final String GET_SYSTEM_STATS = "GET_SYSTEM_STATS";
    public static final String KICK_USER = "KICK_USER";
    
    // Server-to-Client message types
    public static final String USER_JOINED = "USER_JOINED";
    public static final String USER_LEFT = "USER_LEFT";
    public static final String MESSAGE_BROADCAST = "MESSAGE_BROADCAST";
    public static final String PRIVATE_MESSAGE_RECEIVED = "PRIVATE_MESSAGE_RECEIVED";
    public static final String FILE_TRANSFER_REQUEST = "FILE_TRANSFER_REQUEST";
    
    // Additional message types
    public static final String INFO = "INFO";
    public static final String BROADCAST = "BROADCAST";
    public static final String PRIVATE = "PRIVATE";
    public static final String FILE_TRANSFER_ACCEPT = "FILE_TRANSFER_ACCEPT";
    public static final String FILE_TRANSFER_REJECT = "FILE_TRANSFER_REJECT";
    public static final String FILE_METADATA = "FILE_METADATA";
    public static final String FILE_COMPLETE = "FILE_COMPLETE";
    public static final String FILE_ERROR = "FILE_ERROR";
    public static final String FILE_PROGRESS = "FILE_PROGRESS";
    public static final String FILE_TRANSFER_ERROR = "FILE_TRANSFER_ERROR";
    public static final String FILE_TRANSFER_COMPLETE = "FILE_TRANSFER_COMPLETE";
    
    // Admin responses
    public static final String ADMIN_AUTH_SUCCESS = "ADMIN_AUTH_SUCCESS";
    public static final String ADMIN_AUTH_FAILED = "ADMIN_AUTH_FAILED";
    public static final String CONNECTED_CLIENTS_LIST = "CONNECTED_CLIENTS_LIST";
    public static final String CHAT_HISTORY_DATA = "CHAT_HISTORY_DATA";
    public static final String SYSTEM_STATS_DATA = "SYSTEM_STATS_DATA";
    public static final String KICK_SUCCESS = "KICK_SUCCESS";
    public static final String KICK_FAILED = "KICK_FAILED";
    
    // Error types
    public static final String ERROR = "ERROR";
}
