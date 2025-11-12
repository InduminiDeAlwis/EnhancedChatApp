package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message.java
 * Represents a structured message in the chat system
 * Encapsulates all information about a message
 */
public class Message {

    private MessageType type;          // Type of message (BROADCAST, PRIVATE, etc.)
    private String sender;             // Username of sender
    private String recipient;          // Username of recipient (null for broadcast)
    private String content;            // Actual message content
    private LocalDateTime timestamp;   // When message was created

    /**
     * Full constructor with all fields
     */
    public Message(MessageType type, String sender, String recipient, String content) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for broadcast messages (no specific recipient)
     */
    public Message(MessageType type, String sender, String content) {
        this(type, sender, null, content);
    }

    /**
     * Constructor for system messages
     */
    public static Message systemMessage(String content) {
        return new Message(MessageType.SYSTEM, Constants.SYSTEM_SENDER, content);
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Convert message to protocol format for network transmission
     * Format: TYPE|SENDER|RECIPIENT|CONTENT
     * Example: PRIVATE|Alice|Bob|Hey there!
     */
    public String toProtocol() {
        String recipientStr = (recipient != null) ? recipient : "";
        return type.name() + Constants.MESSAGE_DELIMITER +
               sender + Constants.MESSAGE_DELIMITER +
               recipientStr + Constants.MESSAGE_DELIMITER +
               content;
    }

    /**
     * Parse protocol string to create Message object
     * This is the reverse of toProtocol()
     */
    public static Message fromProtocol(String protocolString) {
        try {
            String[] parts = protocolString.split("\\" + Constants.MESSAGE_DELIMITER);

            if (parts.length < 3) {
                return null; // Invalid format
            }

            MessageType type = MessageType.fromString(parts[Constants.MSG_TYPE_INDEX]);
            String sender = parts[Constants.MSG_SENDER_INDEX];
            String recipient = parts.length > Constants.MSG_RECIPIENT_INDEX &&
                              !parts[Constants.MSG_RECIPIENT_INDEX].isEmpty()
                              ? parts[Constants.MSG_RECIPIENT_INDEX]
                              : null;
            String content = parts.length > Constants.MSG_CONTENT_INDEX
                            ? parts[Constants.MSG_CONTENT_INDEX]
                            : "";

            return new Message(type, sender, recipient, content);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Format message for display in UI
     * Examples:
     *   [10:30] Alice: Hello everyone        (broadcast)
     *   [10:31] Alice (private): Hey there   (private)
     *   [10:32] SERVER: User joined          (system)
     */
    public String toDisplayFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeStr = timestamp.format(formatter);

        switch (type) {
            case SYSTEM:
                return "[" + timeStr + "] " + content;
            case PRIVATE:
                return "[" + timeStr + "] " + sender + " (private): " + content;
            case BROADCAST:
                return "[" + timeStr + "] " + sender + ": " + content;
            case JOIN:
            case LEAVE:
                return "[" + timeStr + "] >>> " + content;
            case ERROR:
                return "[" + timeStr + "] ERROR: " + content;
            default:
                return "[" + timeStr + "] " + sender + ": " + content;
        }
    }

    /**
     * Check if this is a private message
     */
    public boolean isPrivate() {
        return type == MessageType.PRIVATE;
    }

    /**
     * Check if this is a system message
     */
    public boolean isSystem() {
        return type == MessageType.SYSTEM;
    }

    /**
     * Check if this message is for a specific user
     */
    public boolean isForUser(String username) {
        return recipient != null && recipient.equals(username);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
