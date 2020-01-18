package co.naes.aurora.transport;

import co.naes.aurora.msg.AuroraInKeyMessage;
import co.naes.aurora.msg.AuroraInMessage;

public interface AuroraIncomingMessageHandler {

    void messageReceived(AuroraInMessage message);

    void keyMessageReceived(AuroraInKeyMessage keyMessage);
}
