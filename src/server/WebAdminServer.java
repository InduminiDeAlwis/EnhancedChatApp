package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple WebSocket server for web-based admin console
 * Implements basic WebSocket protocol (RFC 6455)
 */
public class WebAdminServer implements Runnable {
    private ServerSocket serverSocket;
    private final int port;
    private final Map<String, WebSocketConnection> connections = new ConcurrentHashMap<>();
    private boolean running = false;

    public WebAdminServer(int port) {
        this.port = port;
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("✓ Web Admin Server started on port " + port);
            System.out.println("✓ Access admin console at: http://localhost:" + port + "/admin");
            
            while (running) {
                Socket client = serverSocket.accept();
                new Thread(new WebSocketConnection(client)).start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Web Admin Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class WebSocketConnection implements Runnable {
        private Socket socket;
        private InputStream input;
        private OutputStream output;
        private boolean isWebSocket = false;
        private String sessionId;
        private ClientHandler adminHandler;

        public WebSocketConnection(Socket socket) {
            this.socket = socket;
            this.sessionId = java.util.UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();

                // Read first line to check if it's HTTP or WebSocket upgrade
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                String firstLine = reader.readLine();
                
                if (firstLine == null) return;

                if (firstLine.startsWith("GET /admin")) {
                    // Serve admin HTML page
                    serveAdminPage();
                } else if (firstLine.contains("Upgrade: websocket") || firstLine.startsWith("GET")) {
                    // Handle WebSocket upgrade
                    handleWebSocketUpgrade(reader, firstLine);
                }

            } catch (IOException e) {
                System.err.println("WebSocket connection error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void serveAdminPage() throws IOException {
            // Serve the admin HTML file
            File adminFile = new File("admin/index.html");
            
            if (adminFile.exists()) {
                byte[] content = java.nio.file.Files.readAllBytes(adminFile.toPath());
                
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                
                output.write(response.getBytes(StandardCharsets.UTF_8));
                output.write(content);
                output.flush();
            } else {
                String notFound = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "Admin page not found. Please ensure admin/index.html exists.";
                output.write(notFound.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
        }

        private void handleWebSocketUpgrade(BufferedReader reader, String firstLine) throws IOException {
            String key = null;
            String line;
            
            // Read headers
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Sec-WebSocket-Key:")) {
                    key = line.substring(19).trim();
                }
            }

            if (key == null) {
                return;
            }

            // Send WebSocket handshake response
            String accept = generateAcceptKey(key);
            String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + accept + "\r\n" +
                    "\r\n";
            
            output.write(response.getBytes(StandardCharsets.UTF_8));
            output.flush();

            isWebSocket = true;
            connections.put(sessionId, this);

            // Handle WebSocket messages
            handleWebSocketMessages();
        }

        private String generateAcceptKey(String key) {
            try {
                String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] hash = md.digest((key + magic).getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void handleWebSocketMessages() throws IOException {
            while (isWebSocket && !socket.isClosed()) {
                try {
                    String message = readWebSocketFrame();
                    if (message != null) {
                        handleAdminMessage(message);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        private String readWebSocketFrame() throws IOException {
            int firstByte = input.read();
            if (firstByte == -1) return null;

            int secondByte = input.read();
            if (secondByte == -1) return null;

            boolean masked = (secondByte & 0x80) != 0;
            long payloadLength = secondByte & 0x7F;

            if (payloadLength == 126) {
                payloadLength = (input.read() << 8) | input.read();
            } else if (payloadLength == 127) {
                payloadLength = 0;
                for (int i = 0; i < 8; i++) {
                    payloadLength = (payloadLength << 8) | input.read();
                }
            }

            byte[] maskingKey = new byte[4];
            if (masked) {
                input.read(maskingKey);
            }

            byte[] payload = new byte[(int) payloadLength];
            input.read(payload);

            if (masked) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskingKey[i % 4];
                }
            }

            return new String(payload, StandardCharsets.UTF_8);
        }

        private void sendWebSocketFrame(String message) throws IOException {
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            
            output.write(0x81); // Text frame
            
            if (payload.length <= 125) {
                output.write(payload.length);
            } else if (payload.length <= 65535) {
                output.write(126);
                output.write((payload.length >> 8) & 0xFF);
                output.write(payload.length & 0xFF);
            } else {
                output.write(127);
                for (int i = 7; i >= 0; i--) {
                    output.write((int) ((payload.length >> (i * 8)) & 0xFF));
                }
            }
            
            output.write(payload);
            output.flush();
        }

        private void handleAdminMessage(String jsonMessage) throws IOException {
            try {
                // Parse simple JSON manually (to avoid dependencies)
                String type = extractJsonField(jsonMessage, "type");
                String sender = extractJsonField(jsonMessage, "sender");
                String content = extractJsonField(jsonMessage, "content");

                if (type == null) return;

                switch (type) {
                    case "ADMIN_LOGIN":
                        handleAdminLogin(content);
                        break;
                    case "GET_CONNECTED_CLIENTS":
                        sendConnectedClientsList();
                        break;
                    case "GET_CHAT_HISTORY":
                        sendChatHistory();
                        break;
                    case "GET_SYSTEM_STATS":
                        sendSystemStats();
                        break;
                    case "KICK_USER":
                        handleKickUser(content);
                        break;
                    case "LOGOUT":
                        isWebSocket = false;
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error handling admin message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private String extractJsonField(String json, String field) {
            Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        private void handleAdminLogin(String credentials) throws IOException {
            String[] parts = credentials.split(":");
            
            if (parts.length == 2 && 
                Constants.ADMIN_USERNAME.equals(parts[0]) && 
                Constants.ADMIN_PASSWORD.equals(parts[1])) {
                
                sendJsonMessage("ADMIN_AUTH_SUCCESS", "SERVER", "Admin authentication successful");
            } else {
                sendJsonMessage("ADMIN_AUTH_FAILED", "SERVER", "Invalid admin credentials");
            }
        }

        private void sendConnectedClientsList() throws IOException {
            StringBuilder list = new StringBuilder("=== Connected Clients ===\n");
            java.util.List<String> clients = Server.getConnectedClients();
            if (clients.isEmpty()) {
                list.append("No clients connected");
            } else {
                for (String client : clients) {
                    list.append(client).append("\n");
                }
            }
            list.append("=========================");
            sendJsonMessage("CONNECTED_CLIENTS_LIST", "SERVER", list.toString());
        }

        private void sendChatHistory() throws IOException {
            StringBuilder history = new StringBuilder("=== Chat History ===\n");
            for (Message msg : Server.getChatHistory()) {
                history.append(msg.toString()).append("\n");
            }
            if (history.length() == 19) {
                history.append("No chat history available");
            }
            history.append("====================");
            sendJsonMessage("CHAT_HISTORY_DATA", "SERVER", history.toString());
        }

        private void sendSystemStats() throws IOException {
            String stats = Server.getSystemStats();
            sendJsonMessage("SYSTEM_STATS_DATA", "SERVER", stats);
        }

        private void handleKickUser(String username) throws IOException {
            if (Server.kickUser(username)) {
                sendJsonMessage("KICK_SUCCESS", "SERVER", "User " + username + " has been kicked");
            } else {
                sendJsonMessage("KICK_FAILED", "SERVER", "User " + username + " not found");
            }
        }

        private void sendJsonMessage(String type, String sender, String content) throws IOException {
            String json = String.format(
                "{\"type\":\"%s\",\"sender\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                type, sender, content.replace("\"", "\\\"").replace("\n", "\\n"), 
                java.time.LocalDateTime.now()
            );
            sendWebSocketFrame(json);
        }

        private void cleanup() {
            connections.remove(sessionId);
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        WebAdminServer server = new WebAdminServer(8080);
        server.start();
    }
}
