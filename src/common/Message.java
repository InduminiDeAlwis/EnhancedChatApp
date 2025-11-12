package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    
    public Message(String type, String sender, String receiver, String content) {
/**
 * Represents a message in the Enhanced Chat Application
 * This class encapsulates all information needed for different types of
 * messages
 * including text messages, file transfers, and system notifications
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Message properties
    private MessageType type;
    private String sender;
    private String receiver; // null for broadcast, specific username for private/file transfer
    private String content;
    private String timestamp;

    // File transfer specific properties
    private String filename;
    private long fileSize;
    private String fileId; // Unique identifier for file transfer session

    // Additional data field for extensibility
    private Object data; // For additional data like file info or custom payloads

    /**
     * Constructor for basic text messages
     */
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        this.receiver = null;
    }

    /**
     * Constructor for private messages or targeted messages
     */
    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(String type, String sender, String content) {
        this(type, sender, null, content);
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    @Override
    public String toString() {
        if (receiver != null && !receiver.isEmpty()) {
            return String.format("[%s] [%s → %s]: %s", 
                getFormattedTimestamp(), sender, receiver, content);
        } else {
            return String.format("[%s] [%s → All]: %s", 
                getFormattedTimestamp(), sender, content);
        }
        this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    /**
     * Constructor with additional data field
     */
    public Message(MessageType type, String sender, String receiver, String content, Object data) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    /**
     * Constructor for file transfer messages
     */
    public Message(MessageType type, String sender, String receiver,
            String filename, long fileSize, String fileId) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileId = fileId;
        this.content = String.format("File: %s (%.2f MB)", filename, fileSize / (1024.0 * 1024.0));
        this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    // ============ GETTERS AND SETTERS ============

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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // Legacy support for targetUser (maps to receiver)
    public String getTargetUser() {
        return receiver;
    }

    public void setTargetUser(String targetUser) {
        this.receiver = targetUser;
    }

    // ============ UTILITY METHODS ============

    /**
     * Check if this is a broadcast message (no specific receiver)
     */
    public boolean isBroadcast() {
        return receiver == null || receiver.trim().isEmpty();
    }

    /**
     * Check if this is a private message
     */
    public boolean isPrivate() {
        return receiver != null && !receiver.trim().isEmpty() &&
                (type == MessageType.PRIVATE || type == MessageType.PRIVATE_MESSAGE);
    }

    /**
     * Check if this is a file transfer message
     */
    public boolean isFileTransfer() {
        return type != null && type.isFileTransferMessage();
    }

    /**
     * Format message for display in chat
     */
    public String formatForDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");

        if (isPrivate()) {
            sb.append("[Private from ").append(sender).append("] ");
        } else if (type == MessageType.SYSTEM) {
            sb.append("[SYSTEM] ");
        } else {
            sb.append("[").append(sender).append("] ");
        }

        sb.append(content);
        return sb.toString();
    }

    /**
     * Convert message to protocol string for transmission
     * Format:
     * TYPE|||SENDER|||RECEIVER|||CONTENT|||TIMESTAMP|||FILENAME|||FILESIZE|||FILEID
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name()).append(Constants.MESSAGE_DELIMITER);
        sb.append(sender != null ? sender : "").append(Constants.MESSAGE_DELIMITER);
        sb.append(receiver != null ? receiver : "").append(Constants.MESSAGE_DELIMITER);
        sb.append(content != null ? content : "").append(Constants.MESSAGE_DELIMITER);
        sb.append(timestamp).append(Constants.MESSAGE_DELIMITER);
        sb.append(filename != null ? filename : "").append(Constants.MESSAGE_DELIMITER);
        sb.append(fileSize).append(Constants.MESSAGE_DELIMITER);
        sb.append(fileId != null ? fileId : "");
        return sb.toString();
    }

    /**
     * Parse protocol string to create Message object
     */
    public static Message fromProtocolString(String protocolString) {
        String[] parts = protocolString.split("\\|\\|\\|");

        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid protocol string format");
        }

        MessageType type = MessageType.valueOf(parts[0]);
        String sender = parts[1].isEmpty() ? null : parts[1];
        String receiver = parts[2].isEmpty() ? null : parts[2];
        String content = parts[3].isEmpty() ? null : parts[3];

        Message message;

        // Check if this is a file transfer message
        if (parts.length >= 8 && !parts[5].isEmpty()) {
            String filename = parts[5];
            long fileSize = Long.parseLong(parts[6]);
            String fileId = parts[7].isEmpty() ? null : parts[7];
            message = new Message(type, sender, receiver, filename, fileSize, fileId);
        } else if (receiver != null && !receiver.isEmpty()) {
            message = new Message(type, sender, receiver, content);
        } else {
            message = new Message(type, sender, content);
        }

        // Set timestamp if provided
        if (parts.length > 4 && !parts[4].isEmpty()) {
            message.timestamp = parts[4];
        }

        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s (Type: %s)",
                timestamp, sender, receiver != null ? receiver : "ALL", content, type);
    }
}
