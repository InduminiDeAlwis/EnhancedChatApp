package server;

import common.Message;
import common.MessageType;
import common.Constants;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal WebSocket bridge to accept browser clients and route messages.
 *
 * NOTE: This class requires the `org.java-websocket` library on the classpath.
 * This is an OPTIONAL feature for browser clients. The main chat application
 * works without this using standard Java Sockets.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class WebSocketBridge extends WebSocketServer {
    // Map username -> WebSocket connection
    private final Map<String, WebSocket> userSockets = new ConcurrentHashMap<>();

    public WebSocketBridge(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WS open: " + conn.getRemoteSocketAddress());
        // wait for client to send LOGIN message to associate username
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // remove any mapping that references this connection
        userSockets.entrySet().removeIf(e -> e.getValue().equals(conn));
        System.out.println("WS close: " + conn.getRemoteSocketAddress() + " reason=" + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            Message msg = parseJsonMessage(message);
            if (msg == null)
                return;

            if (msg.getType() == MessageType.LOGIN) {
                // register user
                userSockets.put(msg.getSender(), conn);
                broadcastJson(buildSystemMessageJson(msg.getSender() + " joined."));
                return;
            } else if (msg.getType() == MessageType.LOGOUT) {
                userSockets.remove(msg.getSender());
                broadcastJson(buildSystemMessageJson(msg.getSender() + " left."));
                return;
            } else if (MessageType.BROADCAST.equals(msg.getType())) {
                broadcastJson(toJson(msg));
                return;
            } else if (MessageType.PRIVATE.equals(msg.getType())) {
                String target = msg.getTargetUser();
                WebSocket targetConn = userSockets.get(target);
                if (targetConn != null) {
                    targetConn.send(toJson(msg));
                } else {
                    // send error back to sender
                    conn.send(buildSystemMessageJson("User '" + target + "' not found."));
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(buildSystemMessageJson("Server error: " + e.getMessage()));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocketBridge started on port " + getPort());
    }

    // Very small JSON parser for our message shape. Expects JSON like:
    // {"type":"BROADCAST","sender":"alice","content":"Hello","targetUser":"bob"}
    private Message parseJsonMessage(String json) {
        if (json == null)
            return null;
        String type = extractString(json, "type");
        String sender = extractString(json, "sender");
        String content = extractString(json, "content");
        String target = extractString(json, "targetUser");

        String mt = type != null ? type : MessageType.BROADCAST; // fallback
        Message m = new Message(mt, sender, content);
        if (target != null)
            m.setTargetUser(target);
        return m;
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"");
        Matcher m = p.matcher(json);
        if (m.find())
            return m.group(1);
        return null;
    }

    private String toJson(Message m) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"type\":\"").append(m.getType()).append('\"');
        sb.append(',');
        sb.append("\"sender\":\"").append(escape(m.getSender())).append('\"');
        sb.append(',');
        sb.append("\"content\":\"").append(escape(m.getContent())).append('\"');
        if (m.getTargetUser() != null) {
            sb.append(',');
            sb.append("\"targetUser\":\"").append(escape(m.getTargetUser())).append('\"');
        }
        sb.append('}');
        return sb.toString();
    }

    private Message buildSystemMessage(String text) {
        return new Message(MessageType.BROADCAST, "[system]", text);
    }

    private String buildSystemMessageJson(String text) {
        return toJson(buildSystemMessage(text));
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private void broadcastJson(String json) {
        for (WebSocket ws : userSockets.values()) {
            ws.send(json);
        }
    }

    public static void main(String[] args) throws Exception {
        // Start HTTP file server
        HttpFileServer.startFileServer(Constants.FILE_HTTP_PORT);

        // Start WebSocket server
        WebSocketBridge server = new WebSocketBridge(Constants.WS_PORT);
        server.start();
        System.out.println("WebSocketBridge listening on ws://" + Constants.SERVER_IP + ":" + Constants.WS_PORT);
    }
}
