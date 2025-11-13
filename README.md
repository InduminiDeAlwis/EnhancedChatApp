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
