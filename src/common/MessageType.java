package common;

/**
 * Enumeration of all message types in the Enhanced Chat Application
 * Used to identify and route different types of messages between clients and server
 */
public enum MessageType {
    
    // ============ AUTHENTICATION & CONNECTION ============
    /**
     * Client login request with username
     */
    LOGIN,
    
    /**
     * Successful login response
     */
    LOGIN_SUCCESS,
    
    /**
     * Failed login response
     */
    LOGIN_FAILED,
    
    /**
     * Server response to login (success/failure) - legacy support
     */
    LOGIN_RESPONSE,
    
    /**
     * Client logout/disconnect notification
     */
    LOGOUT,
    
    /**
     * Client disconnect notification - legacy support
     */
    DISCONNECT,
    
    // ============ BASIC CHAT MESSAGES ============
    /**
     * Regular text message broadcast to all clients
     */
    TEXT,
    
    /**
     * Public broadcast message
     */
    PUBLIC_MESSAGE,
    
    /**
     * Broadcast message - legacy support
     */
    BROADCAST,
    
    /**
     * System notification (user joined, left, etc.)
     */
    SYSTEM,
    
    /**
     * Private message between two specific clients
     */
    PRIVATE,
    
    /**
     * Private message - alternative naming
     */
    PRIVATE_MESSAGE,
    
    // ============ FILE TRANSFER MESSAGES ============
    /**
     * Request to send a file to another client
     * Format: FILE_TRANSFER_REQUEST|||sender|||receiver|||filename|||filesize
     */
    FILE_TRANSFER_REQUEST,
    
    /**
     * Response accepting the file transfer
     */
    FILE_TRANSFER_ACCEPT,
    
    /**
     * Response rejecting the file transfer
     */
    FILE_TRANSFER_REJECT,
    
    /**
     * File metadata (name, size, type) before actual transfer
     */
    FILE_METADATA,
    
    /**
     * File transfer start notification
     */
    FILE_TRANSFER_START,
    
    /**
     * Actual file data chunk being transferred
     */
    FILE_CHUNK,
    
    /**
     * File transfer completion notification
     */
    FILE_COMPLETE,
    
    /**
     * File transfer completion - alternative naming
     */
    FILE_TRANSFER_COMPLETE,
    
    /**
     * File transfer error/failure notification
     */
    FILE_ERROR,
    
    /**
     * File transfer error - alternative naming
     */
    FILE_TRANSFER_ERROR,
    
    /**
     * File transfer progress update
     */
    FILE_PROGRESS,
    
    // ============ USER MANAGEMENT ============
    /**
     * Request for list of online users
     */
    USER_LIST_REQUEST,
    
    /**
     * Server response with list of users
     */
    USER_LIST_RESPONSE,
    
    /**
     * User list - alternative naming
     */
    USER_LIST,
    
    /**
     * User joined notification
     */
    USER_JOINED,
    
    /**
     * User left notification
     */
    USER_LEFT,
    
    /**
     * Admin command to kick a user
     */
    KICK_USER,
    
    // ============ SERVER COMMUNICATION ============
    /**
     * Broadcast message from admin
     */
    ADMIN_BROADCAST,
    
    /**
     * Server announcement
     */
    SERVER_ANNOUNCEMENT,
    
    /**
     * Server error message
     */
    SERVER_ERROR,
    
    /**
     * Ping message for connection check
     */
    PING,
    
    /**
     * Pong response to ping
     */
    PONG,
    
    // ============ STATUS & ERROR MESSAGES ============
    /**
     * General success message
     */
    SUCCESS,
    
    /**
     * General error message
     */
    ERROR,
    
    /**
     * Authentication error
     */
    AUTH_ERROR,
    
    /**
     * Information message
     */
    INFO,
    
    /**
     * Invalid message format error
     */
    INVALID_FORMAT;
    
    /**
     * Check if this message type is related to file transfer
     * @return true if this is a file transfer message type
     */
    public boolean isFileTransferMessage() {
        return this == FILE_TRANSFER_REQUEST ||
               this == FILE_TRANSFER_ACCEPT ||
               this == FILE_TRANSFER_REJECT ||
               this == FILE_METADATA ||
               this == FILE_TRANSFER_START ||
               this == FILE_CHUNK ||
               this == FILE_COMPLETE ||
               this == FILE_TRANSFER_COMPLETE ||
               this == FILE_ERROR ||
               this == FILE_TRANSFER_ERROR ||
               this == FILE_PROGRESS;
    }
    
    /**
     * Check if this message type is a system message
     * @return true if this is a system message type
     */
    public boolean isSystemMessage() {
        return this == SYSTEM ||
               this == LOGIN_RESPONSE ||
               this == LOGIN_SUCCESS ||
               this == LOGIN_FAILED ||
               this == USER_LIST_RESPONSE ||
               this == USER_LIST ||
               this == USER_JOINED ||
               this == USER_LEFT ||
               this == SERVER_ANNOUNCEMENT ||
               this == SERVER_ERROR ||
               this == ERROR ||
               this == AUTH_ERROR ||
               this == INFO;
    }
}
