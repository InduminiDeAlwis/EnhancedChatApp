# Server Quick Start Guide

## 🚀 Quick Setup

### 1. Compile
```powershell
cd "c:\Users\Nirasha\Desktop\Enhanced Chat App\EnhancedChatApp"
javac -d bin src/common/*.java src/server/*.java
```

### 2. Run Server
```powershell
java -cp bin server.Server
```

### 3. Use Admin Console
Type commands at the `admin>` prompt:
- `help` - Show all commands
- `list` - View online users
- `stats` - Server statistics
- `logs 20` - View last 20 chat messages
- `announce <message>` - Send announcement
- `kick <username>` - Remove a user
- `shutdown` - Stop server

---

## 📋 Requirements Status

| Requirement | Status |
|------------|--------|
| ✅ Java Sockets & ServerSocket | DONE |
| ✅ Multi-threaded client handling | DONE |
| ✅ Broadcast messaging | DONE |
| ✅ Private messaging | DONE |
| ✅ File transfer support | DONE |
| ✅ Client list maintenance | DONE |
| ✅ Admin console with logs | DONE |

**ALL REQUIREMENTS COMPLETED ✅**

---

## 📁 Files Created

### Common Package (3 files)
- `Constants.java` - Configuration constants
- `MessageType.java` - Message type enums
- `Message.java` - Message data class

### Server Package (5 files)
- `Server.java` - Main server (port 5000)
- `ClientHandler.java` - Per-client thread handler
- `PrivateChatHandler.java` - Private message router
- `AdminConsole.java` - Admin command interface
- `ChatLogger.java` - Logging system

---

## 🎯 Key Features

✨ **Multi-Client Support** (up to 50 concurrent)  
✨ **Public Broadcasting** to all connected users  
✨ **Private Messaging** between specific users  
✨ **File Transfer** coordination  
✨ **Admin Console** with monitoring  
✨ **Chat Logging** to files (`logs/` directory)  
✨ **Thread-Safe** operations  
✨ **Graceful Shutdown**  

---

## 🔧 Configuration

Edit `Constants.java` to change:
- `SERVER_PORT` (default: 5000)
- `MAX_CLIENTS` (default: 50)
- `MAX_MESSAGE_LENGTH` (default: 1000)
- `MAX_FILE_SIZE` (default: 100MB)

---

## 📊 Admin Commands

```
help              Show all commands
list              List online users
stats             Server statistics
announce <msg>    Broadcast to all
kick <user>       Remove a user
logs [n]          View chat logs
serverlogs [n]    View server logs
search <term>     Search logs
clear             Clear screen
shutdown          Stop server
```

---

## 📝 Logs Location

Logs automatically saved to:
- `logs/chat_log.txt` - All chat messages
- `logs/server_log.txt` - Server events

---

## ✅ Testing

**Server starts?** ✅  
**Clients connect?** ✅  
**Messages broadcast?** ✅  
**Private messages?** ✅  
**File transfer?** ✅  
**Admin console?** ✅  
**Logging works?** ✅  

---

## 🎉 Status: COMPLETE

All server requirements implemented and ready for integration testing!
