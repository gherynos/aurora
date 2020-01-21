package co.naes.aurora.transport;

import co.naes.aurora.msg.InKeyMessage;
import co.naes.aurora.msg.InMessage;

public interface AuroraIncomingMessageHandler {

    void messageReceived(InMessage<?> message);

    void keyMessageReceived(InKeyMessage keyMessage);
}
