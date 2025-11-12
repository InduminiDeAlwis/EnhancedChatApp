# 🚀 Quick Start Guide - Admin Console

## Overview
This guide will help you quickly set up and test the Admin Console for the Enhanced Chat System.

## 📋 Prerequisites
- Java JDK 8 or higher installed
- Windows Command Prompt or PowerShell

## ⚡ Fast Setup (3 Steps)

### Step 1: Build the Project
Double-click `build.bat` or run in terminal:
```powershell
.\build.bat
```

### Step 2: Start the Server
Double-click `run-server.bat` or run:
```powershell
.\run-server.bat
```

You should see:
```
==================================================
Enhanced Chat Server Starting...
==================================================
✓ Server started on port 5000
✓ Waiting for client connections...
==================================================
```

### Step 3: Launch Admin Console
Open a NEW terminal and double-click `run-admin.bat` or run:
```powershell
.\run-admin.bat
```

A GUI window will appear!

## 🔐 Login to Admin Console

When the Admin Console opens:
1. Click **"Connect to Server"** button
2. Enter credentials:
   - **Username**: `admin`
   - **Password**: `admin123`
3. Click **OK**

You're now connected! ✅

## 🎮 Testing the Admin Console

### Test 1: Monitor Connected Clients

1. Open **NEW terminals** (as many as you want)
2. In each terminal, run:
   ```powershell
   cd bin
   java client.Client
   ```
3. Enter different usernames (e.g., Alice, Bob, Charlie)
4. Watch them appear in the **"Connected Clients"** tab of Admin Console!

### Test 2: View Chat History

1. In the client terminals, type messages:
   - `Hello everyone!`
   - `How are you?`
   - `/msg Alice Hi there!` (private message)
2. In Admin Console, go to **"Chat History"** tab
3. Click **"🔄 Refresh"**
4. See all messages with timestamps!

### Test 3: Check System Statistics

1. Go to **"System Statistics"** tab in Admin Console
2. Click **"🔄 Refresh"**
3. See:
   - Number of active users
   - Total messages sent
   - Server uptime
   - And more!

### Test 4: Kick a User

1. Go to **"Admin Actions"** tab
2. Type a username (e.g., `Alice`)
3. Click **"❌ Kick User"**
4. Confirm the action
5. User `Alice` will be disconnected!
6. Other clients will see: `<<< Alice has left the chat`

## 📸 What You Should See

### Server Console:
```
[NEW CONNECTION] 127.0.0.1
[USER JOINED] Alice | Total clients: 1
[NEW CONNECTION] 127.0.0.1
[USER JOINED] Bob | Total clients: 2
[ADMIN CONNECTED] 127.0.0.1
```

### Admin Console:
- **Status Bar**: 🟢 Connected to localhost:5000
- **Connected Clients Tab**: Shows Alice, Bob with IPs
- **Chat History**: All messages with timestamps
- **Statistics**: Live server metrics

### Client Console (Alice):
```
✓ Connected to server as: Alice

Commands:
  /msg <user> <message> - Send private message
  /quit - Disconnect
  Just type to send public message

>>> Bob has joined the chat
[Bob]: Hello Alice!
```

## 🎯 Admin Console Features Summary

| Feature | Location | Action |
|---------|----------|--------|
| View Clients | "Connected Clients" tab | Auto-updates |
| Chat History | "Chat History" tab | Click "Refresh" |
| Statistics | "System Statistics" tab | Click "Refresh" |
| Kick User | "Admin Actions" tab | Enter username + click "Kick" |

## 🔄 Auto-Update Features

The Admin Console automatically updates when:
- ✅ A new client connects
- ✅ A client disconnects
- ✅ Messages are sent
- ✅ Statistics change

## 💡 Pro Tips

1. **Multiple Admin Consoles**: You can run multiple admin consoles simultaneously!
2. **Real-time Monitoring**: Keep the Admin Console open while clients chat to see live updates
3. **Chat History Limit**: Only last 100 messages are stored (configurable in Constants.java)
4. **Clean Shutdown**: Always click "Disconnect" before closing Admin Console

## 🐛 Common Issues

### "Connection refused"
- **Solution**: Make sure the server is running first!

### "Invalid admin credentials"
- **Solution**: Use `admin` / `admin123`

### "Port 5000 in use"
- **Solution**: Close other instances or change port in Constants.java

### Admin Console doesn't open
- **Solution**: Make sure you have Java GUI support (not headless JDK)

## 📝 Quick Commands Reference

### Build and Run:
```powershell
# Build
.\build.bat

# Run Server (Terminal 1)
.\run-server.bat

# Run Admin (Terminal 2)
.\run-admin.bat

# Run Client (Terminal 3+)
.\run-client.bat
```

### Manual Compilation:
```powershell
javac -d bin src/common/*.java src/server/*.java src/client/*.java
```

### Manual Execution:
```powershell
cd bin

# Server
java server.Server

# Admin Console
java server.AdminConsole

# Client
java client.Client
```

## 🎓 Learning Points

By using this Admin Console, you're experiencing:
- ✅ Socket-based client-server communication
- ✅ Multi-threaded server handling
- ✅ Object serialization over network
- ✅ Thread-safe collections (ConcurrentHashMap)
- ✅ Swing GUI development
- ✅ Real-time event propagation
- ✅ Admin/User authentication

## 🚀 Next Steps

After testing the basic features:
1. Try connecting 10+ clients
2. Send 100+ messages and check history
3. Test kicking multiple users
4. Monitor statistics during heavy usage
5. Try disconnecting and reconnecting admin console

## 📞 Need Help?

Check the main README.md for:
- Detailed architecture
- Message protocol details
- Security considerations
- Troubleshooting guide
- Future enhancements

---

**Enjoy exploring the Admin Console! 🎉**

Remember: The power is in your hands - use it wisely! 🛡️
