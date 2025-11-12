package client;

import common.Message;

public interface ClientListener {
    void onMessageReceived(Message msg);
}