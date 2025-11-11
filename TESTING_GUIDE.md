# Server Testing Guide

## 🧪 How to Test Your Server Implementation

### **Method 1: Using Test Client (Recommended)**

#### Step 1: Compile Everything
```powershell
cd "c:\Users\Nirasha\Desktop\Enhanced Chat App\EnhancedChatApp"
javac -d bin src/common/*.java src/server/*.java src/test/*.java
```

#### Step 2: Start the Server
Open **Terminal 1** (PowerShell):
```powershell
java -cp bin server.Server
```

You should see:
```
╔══════════════════════════════════════════════════════╗
║   Enhanced Chat Server Started Successfully          ║
╚══════════════════════════════════════════════════════╝
Server listening on port: 5000
Waiting for client connections...

admin>
```

#### Step 3: Start Test Client 1
Open **Terminal 2** (PowerShell):
```powershell
java -cp bin test.TestClient
```

Enter username: **Alice**

#### Step 4: Start Test Client 2
Open **Terminal 3** (PowerShell):
```powershell
java -cp bin test.TestClient
```

Enter username: **Bob**

---

## ✅ **Testing Checklist**

### **Test 1: Server Startup** ✓
- [ ] Server starts without errors
- [ ] Port 5000 is listening
- [ ] Admin console appears

**Command**: Look at Terminal 1
**Expected**: Server running message

---

### **Test 2: Client Connection** ✓
- [ ] Client connects successfully
- [ ] Login request accepted
- [ ] Welcome message received

**In Terminal 2 (Alice)**:
- Should see: `✓ Connected to server!`
- Should see: `✓ Login successful. Welcome Alice!`

**In Terminal 1 (Server)**:
- Should see: `New connection from: 127.0.0.1`
- Should see: `✓ User 'Alice' registered successfully`

---

### **Test 3: Multiple Clients** ✓
- [ ] Second client can connect
- [ ] Both clients stay connected
- [ ] User joined notification

**In Terminal 3 (Bob)**:
- Connect as "Bob"

**In Terminal 2 (Alice)**:
- Should see: `[SYSTEM] Bob has joined the chat`

**In Server Admin Console**:
```
admin> list
```
- Should see both Alice and Bob

---

### **Test 4: Public Broadcasting** ✓
- [ ] Message sent to all clients
- [ ] All clients receive the message

**In Terminal 2 (Alice)**:
```
Alice> Hello everyone!
```

**In Terminal 3 (Bob)**:
- Should see: `[PUBLIC] Alice: Hello everyone!`

**In Server (logs)**:
- Check `logs/chat_log.txt` - message should be logged

---

### **Test 5: Private Messaging** ✓
- [ ] Private message sent to specific user
- [ ] Only recipient receives it

**In Terminal 2 (Alice)**:
```
Alice> @Bob This is a private message
```

**In Terminal 3 (Bob)**:
- Should see: `[PRIVATE from Alice]: This is a private message`

**In Terminal 2 (Alice)**:
- Should see: `→ Private message sent to Bob`

---

### **Test 6: File Transfer Request** ✓
- [ ] File transfer request sent
- [ ] Receiver gets notification

**In Terminal 2 (Alice)**:
```
Alice> /file @Bob
```

**In Terminal 3 (Bob)**:
- Should see: `[FILE TRANSFER] Alice wants to send you: test_file.txt`

---

### **Test 7: User List** ✓
- [ ] Server maintains user list
- [ ] Admin can view users

**In Terminal 1 (Server Admin)**:
```
admin> list
```

**Expected Output**:
```
╔══════════════════════════════════════════════════════╗
║              Online Users (2/50)                     ║
╠══════════════════════════════════════════════════════╣
║  1. Alice                                            ║
║  2. Bob                                              ║
╚══════════════════════════════════════════════════════╝
```

---

### **Test 8: Server Statistics** ✓
- [ ] Server tracks statistics
- [ ] Uptime displayed correctly

**In Terminal 1 (Server Admin)**:
```
admin> stats
```

**Expected Output**:
```
╔══════════════════════════════════════════════════════╗
║              Server Statistics                       ║
╠══════════════════════════════════════════════════════╣
║ Server Port       : 5000                             ║
║ Online Users      : 2                                ║
║ Max Capacity      : 50                               ║
║ Server Uptime     : 5m 32s                           ║
╚══════════════════════════════════════════════════════╝
```

---

### **Test 9: Server Announcements** ✓
- [ ] Admin can broadcast
- [ ] All clients receive announcement

**In Terminal 1 (Server Admin)**:
```
admin> announce Server maintenance in 5 minutes
```

**In Terminals 2 & 3 (Clients)**:
- Should see: `[SERVER ANNOUNCEMENT] Server maintenance in 5 minutes`

---

### **Test 10: Chat Logging** ✓
- [ ] Messages logged to file
- [ ] Admin can view logs

**In Terminal 1 (Server Admin)**:
```
admin> logs 10
```

**Expected**: Last 10 chat messages displayed

**Check Files**:
- `logs/chat_log.txt` - Should contain all messages
- `logs/server_log.txt` - Should contain connection events

---

### **Test 11: Kick User** ✓
- [ ] Admin can kick user
- [ ] User disconnected

**In Terminal 1 (Server Admin)**:
```
admin> kick Bob
```

**In Terminal 3 (Bob)**:
- Should see: `[SERVER ANNOUNCEMENT] You have been kicked...`
- Connection should close

**In Terminal 2 (Alice)**:
- Should see: `[SYSTEM] Bob has left the chat`

---

### **Test 12: Client Disconnect** ✓
- [ ] Client can disconnect gracefully
- [ ] Server removes user from list

**In Terminal 2 (Alice)**:
```
Alice> /quit
```

**Expected**: `✓ Disconnected from server`

**In Terminal 1 (Server)**:
- Should see: `✗ User 'Alice' disconnected`

---

### **Test 13: Duplicate Username** ✓
- [ ] Server rejects duplicate usernames

**In Terminal 2**:
```
java -cp bin test.TestClient
Enter username: Alice
```

**In Terminal 3**:
```
java -cp bin test.TestClient
Enter username: Alice
```

**Expected**: Second Alice should get "Username already taken"

---

### **Test 14: Max Clients** ✓
- [ ] Server enforces client limit

**Check**: In `Constants.java`, `MAX_CLIENTS = 50`

**Test**: Try connecting 51 clients (optional - time consuming)

---

### **Test 15: Graceful Shutdown** ✓
- [ ] Server shuts down properly
- [ ] All clients notified
- [ ] Logs closed

**In Terminal 1 (Server Admin)**:
```
admin> shutdown
Are you sure? yes
```

**In Client Terminals**:
- Should see: `[SERVER ANNOUNCEMENT] Server is shutting down`
- Connection lost

---

## 🎯 **Quick Test Script**

Here's a quick test sequence:

### **Terminal 1 (Server)**:
```powershell
java -cp bin server.Server
```

### **Terminal 2 (Client Alice)**:
```powershell
java -cp bin test.TestClient
# Enter: Alice
# Type: Hello everyone!
# Type: @Bob Hi there
# Type: /quit
```

### **Terminal 3 (Client Bob)**:
```powershell
java -cp bin test.TestClient
# Enter: Bob
# Type: Hi Alice!
# Watch for messages
```

### **Terminal 1 (Admin Commands)**:
```
admin> list
admin> stats
admin> logs 5
admin> announce Testing server
admin> shutdown
```

---

## 📊 **Expected Results Summary**

| Feature | Test Command | Expected Result |
|---------|--------------|-----------------|
| Server Start | `java -cp bin server.Server` | Server starts on port 5000 |
| Client Connect | `java -cp bin test.TestClient` | Login successful |
| Public Message | `Hello everyone!` | All clients see message |
| Private Message | `@Bob Hi` | Only Bob sees message |
| File Transfer | `/file @Bob` | Bob gets request |
| User List | `admin> list` | Shows all users |
| Statistics | `admin> stats` | Shows server stats |
| View Logs | `admin> logs 10` | Shows recent messages |
| Announcement | `admin> announce Test` | All clients notified |
| Kick User | `admin> kick Bob` | Bob disconnected |
| Shutdown | `admin> shutdown` | Clean shutdown |

---

## 🔍 **Debugging Tips**

### **Issue: Server won't start**
```powershell
netstat -ano | findstr :5000
```
- If port is busy, kill the process or change port in Constants.java

### **Issue: Client can't connect**
- Check server is running
- Check port number matches (5000)
- Check firewall settings

### **Issue: Messages not logging**
- Check `logs/` directory exists
- Check file permissions
- Run as administrator if needed

---

## ✅ **Success Indicators**

Your server is working correctly if:
- ✅ Multiple clients can connect
- ✅ Public messages broadcast to all
- ✅ Private messages reach specific users
- ✅ File transfer requests are coordinated
- ✅ Admin console commands work
- ✅ Chat logs are created and viewable
- ✅ Users are tracked correctly
- ✅ Graceful disconnects work
- ✅ No exceptions or errors in console

---

## 🎉 **All Tests Passing = Server is Working Perfectly!**

If all tests pass, your server implementation is **production-ready**!
