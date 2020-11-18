package co.naes.aurora;

import co.naes.aurora.msg.OutMessage;
import co.naes.aurora.msg.in.ConfInMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.in.PublicKeysInMessage;
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

    private final List<byte[]> publicKeys = new ArrayList<>();

    private MockTransport destination;

    private IncomingMessageHandler messageHandler;

    private final String sender;

    private boolean raiseException = false;

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

        if (raiseException) {

            throw new AuroraException("unable to send");
        }

        destination.addKey(key);
    }

    @Override
    public void sendMessage(OutMessage<?> message) throws AuroraException {

        if (raiseException) {

            throw new AuroraException("unable to send");
        }

        destination.addMessage(message);
    }

    @Override
    public void checkForMessages() throws AuroraException {

        if (raiseException) {

            throw new AuroraException("unable to receive");
        }

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

        for (byte[] msg: publicKeys) {

            if (messageHandler.messageReceived(new PublicKeysInMessage(msg))) {

                toRemove.add(msg);
            }
        }
        publicKeys.removeAll(toRemove);
        toRemove.clear();
    }

    @Override
    public boolean requiresArmoredMessages() {

        return false;
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

            case "PK": {

                publicKeys.add(message.getCiphertext());

            } break;
        }
    }

    protected List<byte[]> getParts() {

        return parts;
    }

    protected List<byte[]> getConfs() {

        return confs;
    }

    protected List<byte[]> getKeys() {

        return keys;
    }

    public void setRaiseException(boolean raiseException) {

        this.raiseException = raiseException;
    }
}
