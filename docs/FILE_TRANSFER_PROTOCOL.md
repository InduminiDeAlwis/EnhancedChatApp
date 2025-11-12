# File Transfer Protocol Documentation

## Overview
This document describes the file transfer protocol used in the Enhanced Chat Application. The protocol enables reliable file transfer between clients through the server using TCP sockets.

---

## Protocol Design

### Architecture
```
Client A (Sender) <---> Server <---> Client B (Receiver)
```

The server acts as a relay, routing file transfer messages and data between clients.

---

## Message Flow

### 1. File Transfer Initiation

**Sender → Server:**
```
MessageType: FILE_METADATA
Format: FILE_METADATA|||senderUsername|||receiverUsername|||content|||timestamp|||filename|||fileSize|||fileId
Example: FILE_METADATA|||alice|||bob|||Sending document|||2025-11-11 10:30:00|||report.pdf|||2048576|||uuid-1234
```

**Server → Receiver:**
- Server forwards the FILE_METADATA message to the target receiver
- Receiver displays acceptance dialog

---

### 2. File Transfer Response

**Receiver → Server → Sender:**

**Option A: Accept**
```
MessageType: FILE_TRANSFER_ACCEPT
Format: FILE_TRANSFER_ACCEPT|||receiverUsername|||senderUsername|||Accepted: filename
Example: FILE_TRANSFER_ACCEPT|||bob|||alice|||Accepted: report.pdf
```

**Option B: Reject**
```
MessageType: FILE_TRANSFER_REJECT
Format: FILE_TRANSFER_REJECT|||receiverUsername|||senderUsername|||Rejected: filename
Example: FILE_TRANSFER_REJECT|||bob|||alice|||Rejected: report.pdf
```

---

### 3. File Data Transfer

**After acceptance, sender begins streaming file data:**

1. **Sender writes file size (8 bytes, long)**
   ```java
   dataOut.writeLong(fileSize);
   ```

2. **Sender streams file in chunks**
   ```java
   byte[] buffer = new byte[8192]; // 8KB chunks
   while ((bytesRead = fileInputStream.read(buffer)) != -1) {
       dataOut.write(buffer, 0, bytesRead);
       dataOut.flush();
   }
   ```

3. **Progress updates (optional)**
   ```
   MessageType: FILE_PROGRESS
   Format: FILE_PROGRESS|||sender|||receiver|||percentage%|||timestamp|||filename|||bytesTransferred|||fileSize|||fileId
   ```

---

### 4. Transfer Completion

**Sender → Server → Receiver:**
```
MessageType: FILE_COMPLETE
Format: FILE_COMPLETE|||senderUsername|||receiverUsername|||File sent successfully|||timestamp|||filename|||fileSize|||fileId
Example: FILE_COMPLETE|||alice|||bob|||File sent successfully|||2025-11-11 10:35:00|||report.pdf|||2048576|||uuid-1234
```

---

### 5. Error Handling

**On any error:**
```
MessageType: FILE_ERROR
Format: FILE_ERROR|||username|||target|||error description
Example: FILE_ERROR|||alice|||bob|||Connection lost during transfer
```

---

## Data Structure

### Message Class Properties

```java
class Message {
    MessageType type;           // Type of message
    String sender;              // Sender username
    String receiver;            // Receiver username (null for broadcast)
    String content;             // Message content/description
    String timestamp;           // ISO timestamp
    String filename;            // File name (for file transfers)
    long fileSize;              // File size in bytes
    String fileId;              // Unique transfer session ID
}
```

### Protocol String Format

```
TYPE|||SENDER|||RECEIVER|||CONTENT|||TIMESTAMP|||FILENAME|||FILESIZE|||FILEID
```

**Field Separator:** `|||` (three pipe characters)

---

## File Transfer States

```
PENDING      → Initial state after request sent
ACCEPTED     → Receiver accepted the transfer
REJECTED     → Receiver rejected the transfer
IN_PROGRESS  → File data is being transferred
COMPLETED    → Transfer finished successfully
FAILED       → Transfer failed due to error
```

---

## Technical Specifications

### Buffer Size
- **Chunk Size:** 8192 bytes (8KB)
- **Max File Size:** 100 MB (configurable in Constants)

### Streams Used

**FileSender:**
- `DataOutputStream` - For file size and raw bytes
- `PrintWriter` - For protocol messages
- `FileInputStream` - For reading file
- `BufferedInputStream` - For efficient reading

**FileReceiver:**
- `DataInputStream` - For file size and raw bytes
- `BufferedReader` - For protocol messages
- `FileOutputStream` - For writing file
- `BufferedOutputStream` - For efficient writing

### Socket Configuration
- Connection timeout: 10 seconds
- Transfer timeout: 30 seconds
- Keep-alive: Enabled

---

## Error Scenarios & Handling

| Scenario | Detection | Recovery |
|----------|-----------|----------|
| File not found | Before transfer | Notify user, cancel |
| File too large | Before transfer | Reject with error message |
| Network disconnection | During transfer | Notify both parties, cleanup |
| Disk full | During write | Stop transfer, partial cleanup |
| Receiver offline | On send attempt | Notify sender, queue (future) |
| Duplicate filename | Before save | Auto-rename with suffix |

---

## Security Considerations

### Current Implementation
- File size validation (max 100MB)
- File existence verification
- Read permission checks

### Future Enhancements
- File type whitelist/blacklist
- Virus scanning integration
- Encryption (AES-256)
- Digital signatures for integrity
- User quota limits

---

## Integration with Server

### Server Responsibilities

1. **Message Routing**
   ```java
   if (message.getType().isFileTransferMessage()) {
       routeToClient(message.getReceiver(), message);
   }
   ```

2. **Client State Tracking**
   - Track which clients are in file transfer
   - Prevent multiple simultaneous transfers

3. **Resource Management**
   - Monitor bandwidth usage
   - Implement transfer queues if needed

---

## Testing Checklist

### Unit Tests (Standalone)
- ✅ File reading with chunking
- ✅ File writing with buffering
- ✅ Directory creation
- ✅ Unique filename generation
- ✅ File size validation

### Integration Tests (With Server)
- ⏳ Small file transfer (< 1KB)
- ⏳ Medium file transfer (1-10MB)
- ⏳ Large file transfer (>10MB)
- ⏳ Transfer cancellation
- ⏳ Network interruption recovery
- ⏳ Multiple simultaneous transfers
- ⏳ Special characters in filename

### UI Tests
- ⏳ File selection dialog
- ⏳ Accept/reject dialog
- ⏳ Progress bar updates
- ⏳ Completion notification

---

## Usage Examples

### For Sender (Client A)

```java
// 1. Initialize FileSender
FileSender sender = new FileSender(socket, "alice", new FileSender.FileTransferCallback() {
    @Override
    public void onProgress(int percentage, long sent, long total) {
        System.out.println("Progress: " + percentage + "%");
    }
    
    @Override
    public void onComplete(String filename, long totalBytes) {
        System.out.println("Transfer complete: " + filename);
    }
    
    @Override
    public void onError(String error) {
        System.err.println("Error: " + error);
    }
    
    @Override
    public void onCancelled() {
        System.out.println("Transfer cancelled");
    }
});

// 2. Select and send file
File file = sender.selectFile();
if (file != null) {
    sender.sendFile(file, "bob");
}
```

### For Receiver (Client B)

```java
// 1. Initialize FileReceiver
FileReceiver receiver = new FileReceiver(socket, "bob", new FileReceiver.FileReceiveCallback() {
    @Override
    public void onFileRequest(String sender, String filename, long size) {
        System.out.println("File request from: " + sender);
    }
    
    @Override
    public void onProgress(int percentage, long received, long total) {
        System.out.println("Receiving: " + percentage + "%");
    }
    
    @Override
    public void onComplete(String filename, String savedPath) {
        System.out.println("Saved to: " + savedPath);
    }
    
    @Override
    public void onError(String error) {
        System.err.println("Error: " + error);
    }
    
    @Override
    public void onCancelled() {
        System.out.println("Transfer cancelled");
    }
});

// 2. Handle incoming file (in message listener)
if (message.getType() == MessageType.FILE_METADATA) {
    receiver.receiveFileWithMetadata(message);
}
```

---

## Performance Metrics

### Expected Transfer Speeds
- **Local network (same machine):** ~100-200 MB/s
- **LAN (Gigabit Ethernet):** ~50-100 MB/s
- **WiFi (802.11ac):** ~20-40 MB/s
- **Internet (varied):** Depends on connection

### Resource Usage
- **Memory:** ~10MB per active transfer
- **CPU:** <5% for single transfer
- **Disk I/O:** Depends on disk speed

---

## Future Enhancements

1. **Resume Capability**
   - Save transfer state
   - Resume from last chunk

2. **Multi-part Transfers**
   - Split large files
   - Parallel chunk transfer

3. **Compression**
   - GZIP compression for text files
   - Reduce bandwidth usage

4. **P2P Mode**
   - Direct client-to-client transfer
   - Server only for negotiation

5. **Transfer Queue**
   - Queue multiple files
   - Batch operations

---

## Troubleshooting

### Common Issues

**Problem:** File transfer freezes at 99%
- **Cause:** Flush not called on output stream
- **Solution:** Ensure `flush()` after each chunk

**Problem:** Received file is corrupted
- **Cause:** Byte count mismatch
- **Solution:** Verify file size matches expected size

**Problem:** Transfer very slow
- **Cause:** Small buffer size or no buffering
- **Solution:** Use 8KB+ buffers and BufferedStreams

**Problem:** "File already exists" error
- **Cause:** No duplicate handling
- **Solution:** Auto-rename with counter suffix

---

## Authors
- **Member 4** - File Transfer Feature Implementation
- **Date:** November 11, 2025
- **Version:** 1.0

---

## References
- Java NIO Documentation: https://docs.oracle.com/javase/8/docs/api/java/nio/package-summary.html
- Socket Programming Guide: https://docs.oracle.com/javase/tutorial/networking/sockets/
- File I/O Best Practices: https://docs.oracle.com/javase/tutorial/essential/io/
