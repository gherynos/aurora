package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.msg.OutKeyMessage;
import co.naes.aurora.msg.OutMessage;

public interface AuroraTransport {

    void setIncomingMessageHandler(AuroraIncomingMessageHandler messageHandler);

    void sendKeyMessage(OutKeyMessage key) throws AuroraException;

    void sendMessage(OutMessage<?> message) throws AuroraException;

    void checkForMessages() throws AuroraException;
}
