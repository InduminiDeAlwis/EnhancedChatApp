package common;

import java.io.Serializable;

/**
 * Serializable message used for communication between client and server.
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	private MessageType type;
	private String sender;
	private String content;
	private String targetUser; // optional, used for private messages

	public Message(MessageType type, String sender, String content) {
		this.type = type;
		this.sender = sender;
		this.content = content;
	}

	public MessageType getType() { return type; }
	public String getSender() { return sender; }
	public String getContent() { return content; }

	public String getTargetUser() { return targetUser; }

	public void setTargetUser(String targetUser) { this.targetUser = targetUser; }

	public void setType(MessageType type) { this.type = type; }
	public void setSender(String sender) { this.sender = sender; }
	public void setContent(String content) { this.content = content; }
}
