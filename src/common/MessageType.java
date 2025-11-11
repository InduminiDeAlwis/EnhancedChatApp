package common;

/**
 * Enumeration of all message types in the Enhanced Chat Application
 * Used to identify and route different types of messages between clients and server
 */
public enum MessageType {
    
    // ============ BASIC CHAT MESSAGES ============
    /**
     * Regular text message broadcast to all clients
     */
    TEXT,
    
    /**
     * System notification (user joined, left, etc.)
     */
    SYSTEM,
    
    /**
     * Private message between two specific clients
     */
    PRIVATE,
    
    // ============ CONNECTION MESSAGES ============
    /**
     * Client login request with username
     */
    LOGIN,
    
    /**
     * Server response to login (success/failure)
     */
    LOGIN_RESPONSE,
    
    /**
     * Client disconnect notification
     */
    DISCONNECT,
    
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
     * Actual file data chunk being transferred
     */
    FILE_CHUNK,
    
    /**
     * File transfer completion notification
     */
    FILE_COMPLETE,
    
    /**
     * File transfer error/failure notification
     */
    FILE_ERROR,
    
    /**
     * File transfer progress update
     */
    FILE_PROGRESS,
    
    // ============ ADMIN MESSAGES ============
    /**
     * Request for list of online users
     */
    USER_LIST_REQUEST,
    
    /**
     * Server response with list of users
     */
    USER_LIST_RESPONSE,
    
    /**
     * Admin command to kick a user
     */
    KICK_USER,
    
    /**
     * Broadcast message from admin
     */
    ADMIN_BROADCAST,
    
    // ============ ERROR MESSAGES ============
    /**
     * General error message
     */
    ERROR,
    
    /**
     * Authentication error
     */
    AUTH_ERROR,
    
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
               this == FILE_CHUNK ||
               this == FILE_COMPLETE ||
               this == FILE_ERROR ||
               this == FILE_PROGRESS;
    }
    
    /**
     * Check if this message type is a system message
     * @return true if this is a system message type
     */
    public boolean isSystemMessage() {
        return this == SYSTEM ||
               this == LOGIN_RESPONSE ||
               this == USER_LIST_RESPONSE ||
               this == ERROR ||
               this == AUTH_ERROR;
    }
}
