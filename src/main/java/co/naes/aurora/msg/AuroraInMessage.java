package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import net.nharyes.libsaltpack.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AuroraInMessage extends AuroraMessage {

    private boolean decrypted = false;

    private Object data;

    private PublicKeys sender;

    public AuroraInMessage(byte[] ciphertext) {

        this.ciphertext = ciphertext;
    }

    public void decrypt(AuroraSession session) throws AuroraException {

        try {

            // decrypt message
            ByteArrayInputStream in = new ByteArrayInputStream(ciphertext);
            InputParameters ip = new InputParameters(in);
            ip.setArmored(isArmored());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MessageReader dec = new MessageReader(ip, session.getSecretKey());
            while (dec.hasMoreBlocks())
                out.writeBytes(dec.getBlock());
            sender = new PublicKeys(dec.getSender(), null);
            dec.destroy();

            data = unpackMessage(out.toByteArray());

            decrypted = true;

        } catch (SaltpackException ex) {

            throw new AuroraException("Error while decrypting message: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getSender() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return sender;
    }

    public String getMessageId() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return messageId;
    }

    public boolean isText() {

        return messageType == TYPE_TEXT;
    }

    public boolean isBinary() {

        return messageType == TYPE_BINARY;
    }

    public int getSequenceNumber() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return sequenceNumber;
    }

    public int getTotal() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return total;
    }

    public String getText() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        if (messageType != TYPE_TEXT)
            throw new AuroraException("Wrong message type.");

        return (String) data;
    }

    public byte[] getBinaryData() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        if (messageType != TYPE_BINARY)
            throw new AuroraException("Wrong message type.");

        return (byte[]) data;
    }
}
