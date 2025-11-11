# Enhanced Multi-Client Chat and File Transfer System

## 🎯 Project Overview

The Enhanced Multi-Client Chat and File Transfer System is a Java-based network application that demonstrates core principles of network programming, including socket communication, multi-threading, and data streaming. The system enables multiple clients to connect to a central server, send and receive real-time messages, and features a powerful **Admin Console** for monitoring and management.

## 🏗️ System Architecture

```
EnhancedChatApp/
├── src/
│   ├── common/           # Shared classes
│   │   ├── Constants.java
│   │   ├── Message.java
│   │   └── MessageType.java
│   ├── server/           # Server-side code
│   │   ├── Server.java
│   │   ├── ClientHandler.java
│   │   └── AdminConsole.java    ⭐ Main Admin Module
│   └── client/           # Client-side code
│       ├── Client.java
│       └── ClientListener.java
```

## ✨ Features

### 🛡️ Admin Console Features

1. **Real-time Client Monitoring**
   - View all connected clients with IP addresses
   - Auto-updates when clients join/leave
   - User-friendly tabbed interface

2. **Chat History Viewing**
   - Access last 100 messages
   - Timestamped message log
   - Public and private message tracking

3. **System Statistics Dashboard**
   - Active user count
   - Total messages sent
   - Files transferred
   - Server uptime
   - Admin console connections

4. **User Management**
   - Kick users from the server
   - Real-time confirmation
   - Automatic client list refresh

5. **Secure Authentication**
   - Admin credentials required
   - Encrypted communication
   - Session management

### 🔥 Server Features

- Multi-threaded client handling
- Concurrent user support
- Message broadcasting
- Private messaging support
- Chat history logging (last 100 messages)
- Real-time statistics tracking
- Admin console support

### 💬 Client Features

- Public message broadcasting
- Private messaging
- User join/leave notifications
- Console-based interface

## 🚀 Quick Start Guide

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Command prompt / Terminal

### Step 1: Compile the Project

Navigate to the project root directory and compile all Java files:

```powershell
# Windows PowerShell
javac -d bin src/common/*.java src/server/*.java src/client/*.java

# Or compile individually
javac -d bin src/common/*.java
javac -d bin -cp bin src/server/*.java
javac -d bin -cp bin src/client/*.java
```

### Step 2: Start the Server

```powershell
cd bin
java server.Server
```

Expected output:
```
==================================================
Enhanced Chat Server Starting...
==================================================
✓ Server started on port 5000
✓ Waiting for client connections...
==================================================
```

### Step 3: Launch the Admin Console

Open a **new terminal/command prompt** and run:

```powershell
cd bin
java server.AdminConsole
```

This will open a GUI window with the Admin Console interface.

**Default Admin Credentials:**
- Username: `admin`
- Password: `admin123`

### Step 4: Connect Test Clients

Open **additional terminals** for each test client:

```powershell
cd bin
java client.Client
```

Enter a username when prompted and start chatting!

## 🛡️ Admin Console User Guide

### Main Interface

The Admin Console features a modern tabbed interface with four main sections:

#### 1️⃣ Connected Clients Tab
- **Purpose**: View all active users
- **Features**:
  - Real-time client list with IP addresses
  - Auto-refresh on join/leave events
  - Manual refresh button
- **Display Format**:
  ```
  === Connected Clients ===
  1. user_01 (192.168.1.5)
  2. user_02 (192.168.1.6)
  3. user_03 (192.168.1.8)
  =========================
  ```

#### 2️⃣ Chat History Tab
- **Purpose**: Monitor all conversations
- **Features**:
  - Last 100 messages displayed
  - Timestamp for each message
  - Public and private message tracking
  - Clear display button
- **Display Format**:
  ```
  === Chat History ===
  [2025-11-11 10:15:23] [user_01 → All]: Hello everyone!
  [2025-11-11 10:15:45] [user_02 → user_01]: Hi there!
  [2025-11-11 10:16:12] [user_03 → All]: Good morning!
  ====================
  ```

#### 3️⃣ System Statistics Tab
- **Purpose**: View server performance metrics
- **Display Format**:
  ```
  === System Statistics ===
  Server Start Time: 2025-11-11T10:00:00
  Active Users: 3
  Total Messages Sent: 57
  Files Transferred: 5
  Chat History Size: 57
  Admin Consoles Connected: 1
  ========================
  ```

#### 4️⃣ Admin Actions Tab
- **Purpose**: Perform administrative actions
- **Features**:
  - **Kick User**: Enter username and disconnect them
  - **Instructions**: Usage guidelines
  - **Confirmation**: Prompts before critical actions

### Connection Steps

1. **Click "Connect to Server"** button
2. **Enter credentials** (default: admin/admin123)
3. **Wait for authentication**
4. **Access all features** once connected

### How to Kick a User

1. Go to **"Admin Actions"** tab
2. Enter the **exact username** in the text field
3. Click **"Kick User"** button
4. **Confirm** the action in the popup
5. User will be **disconnected immediately**
6. Client list **auto-refreshes**

## 🔧 Configuration

Edit `src/common/Constants.java` to customize:

```java
public class Constants {
    // Server configuration
    public static final int SERVER_PORT = 5000;
    public static final String SERVER_HOST = "localhost";
    
    // Admin credentials
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    
    // Chat history settings
    public static final int MAX_HISTORY_SIZE = 100;
}
```

## 📋 Message Protocol

The system uses object serialization for communication. All messages use the `Message` class:

```java
Message(String type, String sender, String receiver, String content)
```

### Admin-Specific Message Types

| Message Type | Purpose | Direction |
|--------------|---------|-----------|
| `ADMIN_LOGIN` | Admin authentication | Admin → Server |
| `GET_CONNECTED_CLIENTS` | Request client list | Admin → Server |
| `GET_CHAT_HISTORY` | Request chat logs | Admin → Server |
| `GET_SYSTEM_STATS` | Request statistics | Admin → Server |
| `KICK_USER` | Disconnect a user | Admin → Server |
| `CONNECTED_CLIENTS_LIST` | Client list data | Server → Admin |
| `CHAT_HISTORY_DATA` | Chat history data | Server → Admin |
| `SYSTEM_STATS_DATA` | Statistics data | Server → Admin |

## 🧪 Testing Scenarios

### Test 1: Admin Monitoring
1. Start server
2. Connect admin console
3. Connect 3-4 test clients
4. Verify all clients appear in admin console
5. Check statistics update

### Test 2: Chat History
1. Have clients send various messages
2. Send private messages between users
3. Refresh chat history in admin console
4. Verify all messages are logged with timestamps

### Test 3: User Management
1. Connect multiple clients
2. Select a user from the client list
3. Kick the user via admin console
4. Verify user is disconnected
5. Verify other clients receive notification

### Test 4: Real-time Updates
1. Keep admin console open
2. Have clients join and leave
3. Verify client list updates automatically
4. Check statistics refresh in real-time

## 🔒 Security Considerations

- **Authentication**: Admin credentials required for console access
- **Socket Security**: All communication over TCP
- **Session Management**: Proper connection cleanup on disconnect
- **Input Validation**: Username and message validation
- **Error Handling**: Graceful error recovery

## 🎨 UI Features

The Admin Console includes:
- **Modern GUI**: Professional Swing-based interface
- **Color-coded Status**: Green (connected), Red (error), White (disconnected)
- **Tabbed Navigation**: Easy access to all features
- **Real-time Updates**: Auto-refresh on events
- **Responsive Design**: Adapts to different screen sizes
- **Error Dialogs**: User-friendly error messages

## 🐛 Troubleshooting

### Server won't start
- **Check port**: Ensure port 5000 is not in use
- **Firewall**: Allow Java through firewall
- **Permissions**: Run with appropriate permissions

### Admin can't connect
- **Server running**: Verify server is active
- **Credentials**: Use correct username/password
- **Network**: Check localhost connectivity

### Clients disconnecting
- **Network stability**: Check connection quality
- **Server load**: Monitor resource usage
- **Error logs**: Check server console for errors

## 📚 Learning Objectives Demonstrated

✅ **Real-time client-server communication** using Java sockets  
✅ **Multi-threading** for concurrent client handling  
✅ **Object serialization** for data transfer  
✅ **Thread-safe collections** (ConcurrentHashMap, CopyOnWriteArrayList)  
✅ **Swing GUI development** for admin interface  
✅ **Network protocol design** with message types  
✅ **Error handling and recovery** mechanisms  
✅ **Admin/user separation** with authentication  

## 🚧 Future Enhancements

- [ ] File transfer monitoring in admin console
- [ ] Export chat history to file
- [ ] Ban user functionality with time limits
- [ ] Broadcast admin messages to all users
- [ ] GUI-based client application
- [ ] End-to-end message encryption
- [ ] Database integration for persistent storage
- [ ] Web-based admin dashboard

## 📝 Code Structure

### Key Classes

**Server.java**
- Main server entry point
- Manages client connections
- Handles admin console connections
- Maintains chat history and statistics

**ClientHandler.java**
- Handles individual client connections
- Processes messages
- Manages authentication
- Handles admin requests

**AdminConsole.java**
- GUI-based admin interface
- Real-time monitoring
- User management features
- Statistics display

**Message.java**
- Universal message format
- Serializable for network transfer
- Timestamp support

**MessageType.java**
- Defines all message types
- Constants for client, server, and admin messages

## 👥 Author

Built as part of the Enhanced Multi-Client Chat and File Transfer System project, demonstrating advanced network programming concepts in Java.

## 📄 License

This project is for educational purposes.

---

**Need Help?** Check the troubleshooting section or review the inline code comments for detailed explanations.

**Happy Chatting! 🎉**