package client;

import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ClientListener implements Runnable {
    private ObjectInputStream input;
    private boolean running = true;
    
    public ClientListener(ObjectInputStream input) {
        this.input = input;
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case MessageType.MESSAGE_BROADCAST:
                System.out.println("[" + message.getSender() + "]: " + message.getContent());
                break;
                
            case MessageType.PRIVATE_MESSAGE_RECEIVED:
                System.out.println("[Private from " + message.getSender() + "]: " + message.getContent());
                break;
                
            case MessageType.USER_JOINED:
                System.out.println(">>> " + message.getContent());
                break;
                
            case MessageType.USER_LEFT:
                System.out.println("<<< " + message.getContent());
                break;
                
            case MessageType.ERROR:
                System.err.println("Error: " + message.getContent());
                break;
                
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }
    
    public void stop() {
        running = false;
    }
}
