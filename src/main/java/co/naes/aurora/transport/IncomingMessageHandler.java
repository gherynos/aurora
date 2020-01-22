package co.naes.aurora.transport;

import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;

public interface IncomingMessageHandler {

    void messageReceived(InMessage<?> message);

    void keyMessageReceived(InKeyMessage keyMessage);
}
