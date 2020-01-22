package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.msg.OutMessage;

public interface AuroraTransport {

    void setIncomingMessageHandler(IncomingMessageHandler messageHandler);

    void sendKeyMessage(OutKeyMessage key) throws AuroraException;

    void sendMessage(OutMessage<?> message) throws AuroraException;

    void checkForMessages() throws AuroraException;
}
