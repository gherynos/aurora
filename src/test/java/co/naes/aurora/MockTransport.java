package co.naes.aurora;

import co.naes.aurora.msg.OutMessage;
import co.naes.aurora.msg.in.ConfInMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.IncomingMessageHandler;

import java.util.ArrayList;
import java.util.List;

public class MockTransport implements AuroraTransport {

    private final List<byte[]> keys = new ArrayList<>();

    private final List<byte[]> parts = new ArrayList<>();

    private final List<byte[]> confs = new ArrayList<>();

    private MockTransport destination;

    private IncomingMessageHandler messageHandler;

    private final String sender;

    protected MockTransport(String sender) {

        this.sender = sender;
    }

    protected void setDestination(MockTransport destination) {

        this.destination = destination;
    }

    @Override
    public void setIncomingMessageHandler(IncomingMessageHandler messageHandler) {

        this.messageHandler = messageHandler;
    }

    @Override
    public void sendKeyMessage(OutKeyMessage key) throws AuroraException {

        destination.addKey(key);
    }

    @Override
    public void sendMessage(OutMessage<?> message) throws AuroraException {

        destination.addMessage(message);
    }

    @Override
    public void checkForMessages() throws AuroraException {

        List<byte[]> toRemove = new ArrayList<>();
        for (byte[] msg: keys) {

            if (messageHandler.keyMessageReceived(new InKeyMessage(msg, sender))) {

                toRemove.add(msg);
            }
        }
        keys.removeAll(toRemove);
        toRemove.clear();

        for (byte[] msg: parts) {

            if (messageHandler.messageReceived(new PartInMessage(msg))) {

                toRemove.add(msg);
            }
        }
        parts.removeAll(toRemove);
        toRemove.clear();

        for (byte[] msg: confs) {

            if (messageHandler.messageReceived(new ConfInMessage(msg))) {

                toRemove.add(msg);
            }
        }
        confs.removeAll(toRemove);
        toRemove.clear();
    }

    protected void addKey(OutKeyMessage key) {

        keys.add(key.getCiphertext());
    }

    protected void addMessage(OutMessage<?> message) throws AuroraException {

        switch (OutMessage.getIdentifier(message.getClass())) {

            case "Part": {

                parts.add(message.getCiphertext());

            } break;

            case "Conf": {

                confs.add(message.getCiphertext());

            } break;
        }
    }

    protected int getPartsSize() {

        return parts.size();
    }

    protected int getConfsSize() {

        return confs.size();
    }

    protected int getKeysSize() {

        return keys.size();
    }
}
