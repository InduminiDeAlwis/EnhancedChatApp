package common;

/**
 * MessageType.java
 * Enum defining different types of messages in the chat system
 * This helps both client and server understand what kind of message is being sent
 */
public enum MessageType {

    /**
     * BROADCAST - Message sent to all connected users
     * Example: "Hello everyone!" goes to all clients
     */
    BROADCAST,

    /**
     * PRIVATE - Message sent to specific user only
     * Example: Alice sends "Hi" privately to Bob
     * This is your feature!
     */
    PRIVATE,

    /**
     * SYSTEM - System notification from server
     * Example: "User Alice joined the chat"
     */
    SYSTEM,

    /**
     * FILE - File transfer message
     * Example: Alice sends document.pdf to Bob
     */
    FILE,

    /**
     * USER_LIST - Server sends list of online users
     * Example: Response to /users command
     */
    USER_LIST,

    /**
     * JOIN - User joining the chat
     * Example: When client first connects
     */
    JOIN,

    /**
     * LEAVE - User leaving the chat
     * Example: When client disconnects
     */
    LEAVE,

    /**
     * ERROR - Error message from server
     * Example: "Username already taken"
     */
    ERROR,

    /**
     * PRIVATE_REQUEST - Request to start private chat
     * Example: Alice wants to open private chat with Bob
     */
    PRIVATE_REQUEST,

    /**
     * PRIVATE_ACCEPT - Acceptance of private chat request
     * Example: Bob accepts Alice's private chat request
     */
    PRIVATE_ACCEPT;

    /**
     * Convert string to MessageType
     * Useful when parsing received messages
     */
    public static MessageType fromString(String type) {
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If unknown type, default to BROADCAST
            return BROADCAST;
        }
    }

    /**
     * Check if this message type requires a recipient
     * PRIVATE and FILE need a specific recipient
     */
    public boolean requiresRecipient() {
        return this == PRIVATE || this == FILE || this == PRIVATE_REQUEST;
    }
}
