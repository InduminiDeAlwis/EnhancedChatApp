package server;

import common.Constants;
import common.Message;
import common.MessageType;

/**
 * Handles private chat and file transfer operations between clients
 */
public class PrivateChatHandler {
    private final Server server;

    public PrivateChatHandler(Server server) {
        this.server = server;
    }

    /**
     * Route a private message from sender to receiver
     */
    public boolean routePrivateMessage(Message message) {
        String sender = message.getSender();
        String receiver = message.getReceiver();

        // Validate sender and receiver
        if (!validateUsers(sender, receiver)) {
            return false;
        }

        // Get receiver's handler
        ClientHandler receiverHandler = server.getClientHandler(receiver);
        if (receiverHandler == null || !receiverHandler.isRunning()) {
            System.out.println("Private message failed: User '" + receiver + "' not found or offline");
            return false;
        }

        // Send message to receiver
        receiverHandler.sendMessage(message);

        // Send confirmation to sender
        ClientHandler senderHandler = server.getClientHandler(sender);
        if (senderHandler != null) {
            Message confirmation = new Message(MessageType.INFO, Constants.SERVER_NAME,
                    "Private message sent to " + receiver);
            senderHandler.sendMessage(confirmation);
        }

        System.out.println("Private message routed: " + sender + " -> " + receiver);
        return true;
    }

    /**
     * Handle file transfer operations
     */
    public void handleFileTransfer(Message message) {
        String sender = message.getSender();
        String receiver = message.getReceiver();
        MessageType type = message.getType();

        System.out.println("File transfer " + type + ": " + sender + " -> " + receiver);

        // Validate users
        if (!validateUsers(sender, receiver)) {
            notifyFileTransferError(sender, "Invalid sender or receiver");
            return;
        }

        ClientHandler receiverHandler = server.getClientHandler(receiver);
        if (receiverHandler == null || !receiverHandler.isRunning()) {
            notifyFileTransferError(sender, "User '" + receiver + "' is not online");
            return;
        }

        switch (type) {
            case FILE_TRANSFER_REQUEST:
                handleFileTransferRequest(message, receiverHandler);
                break;

            case FILE_TRANSFER_ACCEPT:
                handleFileTransferAccept(message, receiverHandler);
                break;

            case FILE_TRANSFER_REJECT:
                handleFileTransferReject(message, receiverHandler);
                break;

            case FILE_METADATA:
                handleFileMetadata(message, receiverHandler);
                break;

            case FILE_COMPLETE:
                handleFileComplete(message, receiverHandler);
                break;

            case FILE_ERROR:
                handleFileError(message, receiverHandler);
                break;

            case FILE_PROGRESS:
                handleFileProgress(message, receiverHandler);
                break;

            default:
                System.out.println("Unhandled file transfer type: " + type);
                break;
        }
    }

    /**
     * Handle file transfer request
     */
    private void handleFileTransferRequest(Message message, ClientHandler receiverHandler) {
        // Forward the request to the receiver
        receiverHandler.sendMessage(message);

        // Notify sender that request was sent
        ClientHandler senderHandler = server.getClientHandler(message.getSender());
        if (senderHandler != null) {
            Message notification = new Message(MessageType.INFO, Constants.SERVER_NAME,
                    "File transfer request sent to " + message.getReceiver());
            senderHandler.sendMessage(notification);
        }

        // Log file transfer request
        server.getChatLogger().logFileTransfer(
                message.getSender(),
                message.getReceiver(),
                message.getContent(),
                "REQUESTED");

        System.out.println("File transfer request forwarded: " + message.getSender() +
                " -> " + message.getReceiver());
    }

    /**
     * Handle file transfer acceptance
     */
    private void handleFileTransferAccept(Message message, ClientHandler receiverHandler) {
        // The receiver here is actually the original sender
        // Forward the acceptance to the original sender
        receiverHandler.sendMessage(message);

        // Log file transfer acceptance
        server.getChatLogger().logFileTransfer(
                message.getReceiver(),
                message.getSender(),
                message.getContent(),
                "ACCEPTED");

        System.out.println("File transfer accepted: " + message.getReceiver() +
                " accepted from " + message.getSender());
    }

    /**
     * Handle file transfer rejection
     */
    private void handleFileTransferReject(Message message, ClientHandler receiverHandler) {
        // Forward the rejection to the original sender
        receiverHandler.sendMessage(message);

        // Log file transfer rejection
        server.getChatLogger().logFileTransfer(
                message.getReceiver(),
                message.getSender(),
                message.getContent(),
                "REJECTED");

        System.out.println("File transfer rejected: " + message.getReceiver() +
                " rejected from " + message.getSender());
    }

    /**
     * Handle file metadata message
     * This is sent before the actual file data transfer begins
     */
    private void handleFileMetadata(Message message, ClientHandler receiverHandler) {
        // Forward the metadata to the receiver
        receiverHandler.sendMessage(message);

        // Log file metadata
        String filename = message.getFilename();
        long fileSize = message.getFileSize();
        server.getChatLogger().logFileTransfer(
                message.getSender(),
                message.getReceiver(),
                filename + " (" + formatFileSize(fileSize) + ")",
                "METADATA_SENT");

        System.out.println("File metadata forwarded: " + filename +
                " (" + formatFileSize(fileSize) + ") from " +
                message.getSender() + " to " + message.getReceiver());
    }

    /**
     * Handle file transfer completion notification
     */
    private void handleFileComplete(Message message, ClientHandler receiverHandler) {
        // Forward completion notification to the receiver
        receiverHandler.sendMessage(message);

        // Log file transfer completion
        server.getChatLogger().logFileTransfer(
                message.getSender(),
                message.getReceiver(),
                message.getContent(),
                "COMPLETED");

        System.out.println("File transfer completed: " + message.getSender() +
                " -> " + message.getReceiver());
    }

    /**
     * Handle file transfer error notification
     */
    private void handleFileError(Message message, ClientHandler receiverHandler) {
        // Forward error notification to the receiver
        receiverHandler.sendMessage(message);

        // Log file transfer error
        server.getChatLogger().logFileTransfer(
                message.getSender(),
                message.getReceiver(),
                message.getContent(),
                "ERROR");

        System.err.println("File transfer error: " + message.getSender() +
                " -> " + message.getReceiver() + ": " + message.getContent());
    }

    /**
     * Handle file transfer progress update
     */
    private void handleFileProgress(Message message, ClientHandler receiverHandler) {
        // Optionally forward progress to receiver (usually not needed)
        // receiverHandler.sendMessage(message);

        // Log progress if needed
        System.out.println("File transfer progress: " + message.getSender() +
                " -> " + message.getReceiver() + ": " + message.getContent());
    }

    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Validate that sender and receiver exist
     */
    private boolean validateUsers(String sender, String receiver) {
        if (sender == null || sender.trim().isEmpty()) {
            System.err.println("Invalid sender");
            return false;
        }

        if (receiver == null || receiver.trim().isEmpty()) {
            System.err.println("Invalid receiver");
            return false;
        }

        if (sender.equals(receiver)) {
            System.err.println("Sender and receiver cannot be the same");
            return false;
        }

        return true;
    }

    /**
     * Notify sender of file transfer error
     */
    private void notifyFileTransferError(String username, String errorMessage) {
        ClientHandler handler = server.getClientHandler(username);
        if (handler != null) {
            Message error = new Message(MessageType.FILE_TRANSFER_ERROR,
                    Constants.SERVER_NAME, errorMessage);
            handler.sendMessage(error);
        }
    }

    /**
     * Notify both parties of successful file transfer
     */
    public void notifyFileTransferComplete(String sender, String receiver, String filename) {
        ClientHandler senderHandler = server.getClientHandler(sender);
        ClientHandler receiverHandler = server.getClientHandler(receiver);

        if (senderHandler != null) {
            Message msg = new Message(MessageType.FILE_TRANSFER_COMPLETE,
                    Constants.SERVER_NAME, "File '" + filename + "' sent successfully to " + receiver);
            senderHandler.sendMessage(msg);
        }

        if (receiverHandler != null) {
            Message msg = new Message(MessageType.FILE_TRANSFER_COMPLETE,
                    Constants.SERVER_NAME, "File '" + filename + "' received successfully from " + sender);
            receiverHandler.sendMessage(msg);
        }

        // Log file transfer completion
        server.getChatLogger().logFileTransfer(sender, receiver, filename, "COMPLETED");

        System.out.println("File transfer completed: " + filename + " (" + sender +
                " -> " + receiver + ")");
    }
}
