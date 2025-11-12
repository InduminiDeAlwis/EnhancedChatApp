package server;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Small WebSocket client to test the WebSocketBridge server.
 */
public class TestWsClient {
    public static void main(String[] args) throws Exception {
        URI uri = new URI("ws://127.0.0.1:8080");
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("WS client opened");
                // send login message
                String login = "{\"type\":\"LOGIN\",\"sender\":\"testuser\",\"content\":\"\"}";
                send(login);
            }

            @Override
            public void onMessage(String message) {
                System.out.println("WS client got: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("WS client closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        client.connectBlocking();
        // keep alive briefly to receive messages
        Thread.sleep(3000);
        client.close();
    }
}
