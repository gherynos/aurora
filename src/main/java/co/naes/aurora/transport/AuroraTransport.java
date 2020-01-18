package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.msg.AuroraOutKeyMessage;
import co.naes.aurora.msg.AuroraOutMessage;

public interface AuroraTransport {

    void setIncomingMessageHandler(AuroraIncomingMessageHandler messageHandler);

    void sendKeyMessage(AuroraOutKeyMessage key) throws AuroraException;

    void sendMessage(AuroraOutMessage message) throws AuroraException;

    void checkForMessages() throws AuroraException;
}
