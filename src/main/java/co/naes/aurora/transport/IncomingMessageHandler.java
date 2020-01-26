package co.naes.aurora.transport;

import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;

public interface IncomingMessageHandler {

    boolean messageReceived(InMessage<?> message);

    boolean keyMessageReceived(InKeyMessage keyMessage);
}
