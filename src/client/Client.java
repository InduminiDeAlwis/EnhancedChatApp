package client;

import common.Constants;
import common.Message;
import common.MessageType;
import java.io.*;
import java.net.Socket;

public class Client {
    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(String username) {
        this.username = username;
    }

    public boolean connect() {
        try {
            socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Send login message to server
            Message loginMsg = new Message(MessageType.LOGIN, username, null);
            out.writeObject(loginMsg);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    // Pass message to listener/UI
                    System.out.println("Received: " + msg.getContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getUsername() {
        return username;
    }
}