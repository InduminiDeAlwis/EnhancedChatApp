# Web-Based Admin Console

## Overview
The Enhanced Chat System now includes a modern web-based admin console that you can access from any browser!

## Features
✨ **Modern Web UI** - Beautiful, responsive design that works on desktop and mobile  
🔐 **Secure Authentication** - Admin login with username/password  
📊 **Real-time Dashboard** - View connected clients, chat history, and system statistics  
⚙️ **Admin Actions** - Kick users and manage the server  
🔄 **Auto-refresh** - Data updates automatically every 5 seconds  

## Quick Start

### 1. Start the Server
```bash
run-server-web.bat
```
Or manually:
```bash
java -cp bin server.Server
```

### 2. Access Admin Console
Open your web browser and navigate to:
```
http://localhost:8080/admin
```

### 3. Login
Use these credentials:
- **Username**: `admin`
- **Password**: `admin123`
- **Server Host**: `localhost`
- **Server Port**: `5001` (WebSocket port)

## Features in Detail

### 📋 Connected Clients Tab
- View all currently connected users
- See their connection details
- Real-time updates when users join/leave

### 💬 Chat History Tab
- Browse recent chat messages
- View public and private messages
- Refresh to get latest messages

### 📊 Statistics Tab
- Server uptime
- Total users online
- Messages sent
- Files transferred
- Chat history size

### ⚙️ Actions Tab
- **Kick User**: Disconnect specific users from the server
- Get immediate feedback on actions

## Technical Details

### Architecture
- **WebSocket Communication**: Real-time bidirectional communication
- **No External Dependencies**: Pure Java implementation of WebSocket protocol (RFC 6455)
- **Lightweight**: Minimal overhead, runs alongside main chat server
- **Port Configuration**: 
  - Main Chat Server: 5000
  - WebSocket Admin Server: 8080

### How It Works
1. WebAdminServer listens on port 8080
2. Serves the HTML/JavaScript admin interface
3. Upgrades HTTP connections to WebSocket for real-time communication
4. Communicates with main Server class for data and actions

### Files
- `admin/index.html` - Web admin console interface
- `src/server/WebAdminServer.java` - WebSocket server implementation
- `src/server/Server.java` - Main server (updated to start WebAdminServer)
- `run-server-web.bat` - Convenient startup script

## Advantages Over Desktop GUI

✅ **No Installation Required** - Just open a browser  
✅ **Cross-Platform** - Works on Windows, Mac, Linux  
✅ **Remote Access** - Can manage server from anywhere on the network  
✅ **Mobile Friendly** - Responsive design works on tablets and phones  
✅ **Multiple Admins** - Several admins can connect simultaneously  
✅ **No Java GUI Dependencies** - No Swing/AWT required  

## Customization

### Change Admin Credentials
Edit `src/common/Constants.java`:
```java
public static final String ADMIN_USERNAME = "admin";
public static final String ADMIN_PASSWORD = "admin123";
```

### Change Port
Edit `src/server/Server.java`:
```java
WebAdminServer webAdmin = new WebAdminServer(8080); // Change 8080 to your port
```

### Customize UI
Edit `admin/index.html` - Modify colors, layout, or add new features!

## Troubleshooting

### Can't access http://localhost:8080/admin
- Ensure the server is running
- Check if port 8080 is available
- Try `http://127.0.0.1:8080/admin` instead

### "Connection failed" error
- Make sure Server is running on port 5001
- Check firewall settings
- Verify WebSocket port (5001) is correct in login form

### Admin login fails
- Verify username is `admin` and password is `admin123`
- Check server console for error messages
- Ensure you compiled latest code

## Browser Compatibility
- ✅ Chrome/Edge (Recommended)
- ✅ Firefox
- ✅ Safari
- ✅ Opera
- ✅ Mobile browsers

## Security Notes
⚠️ **For Development Only**: This implementation is designed for local development and testing.

For production use, consider:
- HTTPS/WSS (secure WebSocket)
- Stronger authentication
- Session management
- Rate limiting
- Input validation
- CORS restrictions

## Future Enhancements
- [ ] Multi-admin support with different roles
- [ ] Real-time charts and graphs
- [ ] Export chat history
- [ ] Server configuration via UI
- [ ] User ban/unban functionality
- [ ] Broadcast announcements
- [ ] Server restart/shutdown controls

---

**Enjoy managing your chat server from the comfort of your browser!** 🚀
