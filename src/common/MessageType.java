package common;

/**
 * Enum representing different types of messages in the chat system
 */
public enum MessageType {
    // Authentication
    LOGIN,
    LOGOUT,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    
    // Messaging
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    
    // File Transfer
    FILE_TRANSFER_REQUEST,
    FILE_TRANSFER_ACCEPT,
    FILE_TRANSFER_REJECT,
    FILE_TRANSFER_START,
    FILE_TRANSFER_COMPLETE,
    FILE_TRANSFER_ERROR,
    
    // User Management
    USER_LIST,
    USER_JOINED,
    USER_LEFT,
    
    // Server Communication
    SERVER_ANNOUNCEMENT,
    SERVER_ERROR,
    PING,
    PONG,
    
    // Status
    SUCCESS,
    ERROR,
    INFO
}
