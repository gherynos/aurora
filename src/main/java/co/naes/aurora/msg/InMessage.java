package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import net.nharyes.libsaltpack.InputParameters;
import net.nharyes.libsaltpack.MessageReader;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class InMessage<T> extends CiphertextMessage {

    protected short messageType;

    protected boolean decrypted = false;

    protected PublicKeys sender;

    private T data;

    protected abstract T unpackData(MessageUnpacker unpacker) throws IOException;

    public InMessage(byte[] ciphertext) {

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

            // unpack data
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(out.toByteArray());
            data = unpackData(unpacker);
            unpacker.close();

            decrypted = true;

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while decrypting message: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getSender() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return sender;
    }

    public T getData() throws AuroraException {

        if (!decrypted)
            throw new AuroraException("Decrypt message first.");

        return data;
    }

    public short getMessageType() {

        return messageType;
    }
}
