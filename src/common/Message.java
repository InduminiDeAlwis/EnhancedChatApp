package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message class representing a message in the chat system
 * Implements Serializable for network transmission
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private String timestamp;
    private Object data; // For additional data like file info
    
    // Constructors
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = getCurrentTimestamp();
    }
    
    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = getCurrentTimestamp();
    }
    
    public Message(MessageType type, String sender, String receiver, String content, Object data) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.data = data;
        this.timestamp = getCurrentTimestamp();
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
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    // Helper method to get current timestamp
    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s (Type: %s)", 
            timestamp, sender, receiver != null ? receiver : "ALL", content, type);
    }
}
