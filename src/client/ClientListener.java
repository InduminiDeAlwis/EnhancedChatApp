package client;

import common.Message;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * ClientListener.java
 * Background thread that continuously listens for messages from server
 * Runs separately from the main UI thread so UI doesn't freeze
 */
public class ClientListener extends Thread {

    private BufferedReader in;
    private Client.MessageHandler handler;
    private boolean listening;

    /**
     * Constructor
     * @param in Input stream from server
     * @param handler Callback interface to handle received messages
     */
    public ClientListener(BufferedReader in, Client.MessageHandler handler) {
        this.in = in;
        this.handler = handler;
        this.listening = true;
    }

    /**
     * Main thread execution
     * Continuously reads messages from server
     */
    @Override
    public void run() {
        String receivedData;

        try {
            while (listening && (receivedData = in.readLine()) != null) {
                try {
                    // Parse received message
                    Message message = Message.fromProtocol(receivedData);

                    if (message != null) {
                        // Pass message to handler (usually a UI component)
                        if (handler != null) {
                            handler.onMessageReceived(message);
                        }
                    } else {
                        System.err.println("Received invalid message format");
                    }

                } catch (Exception e) {
                    System.err.println("Error processing received message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if (listening) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        } finally {
            System.out.println("ClientListener stopped");
        }
    }

    /**
     * Stop listening for messages
     */
    public void stopListening() {
        listening = false;
        this.interrupt();
    }

    /**
     * Check if still listening
     */
    public boolean isListening() {
        return listening;
    }
}
