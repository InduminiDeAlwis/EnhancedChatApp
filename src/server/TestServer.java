package server;

import common.Constants;
import common.Message;
import common.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Small test server stub used to validate the client during development.
 * It accepts one client connection, prints incoming messages and replies with an acknowledgement.
 */
public class TestServer {
    public static void main(String[] args) {
        System.out.println("TestServer starting on port " + Constants.SERVER_PORT);
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            while (true) {
                System.out.println("Waiting for client...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected from " + socket.getRemoteSocketAddress());

                Thread t = new Thread(() -> handleClient(socket));
                t.setDaemon(true);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Read messages and echo a simple acknowledgement
            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) {
                    System.out.println("Received unknown object: " + obj);
                    continue;
                }
                Message msg = (Message) obj;
                System.out.println("Received [" + msg.getType() + "] from " + msg.getSender() + ": " + msg.getContent());

                // send back an ack message
                Message ack = new Message(MessageType.BROADCAST, "Server", "ACK: received " + msg.getType());
                out.writeObject(ack);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client handler terminated: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
