package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import net.nharyes.libsaltpack.MessageWriter;
import net.nharyes.libsaltpack.OutputParameters;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class OutMessage<T> extends CiphertextMessage {

    protected PublicKeys recipient;

    protected abstract void packData(MessageBufferPacker packer, T data) throws IOException;

    public OutMessage(AuroraSession session, PublicKeys recipient, T data, boolean armored) throws AuroraException {

        // encrypt message
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            byte[][] recipients = {recipient.getPublicKey()};

            OutputParameters op = new OutputParameters(out);
            op.setArmored(armored);
            if (armored) {

                op.setApp(APP);
                op.setLettersInWords(15);
                op.setWordsInPhrase(5);
            }

            // create binary data
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packData(packer, data);
            packer.close();

            MessageWriter enc = new MessageWriter(op, session.getSecretKey(), recipients);
            enc.addBlock(packer.toByteArray(), true); // TODO: check size and split blocks

            out.flush();
            enc.destroy();

            ciphertext = out.toByteArray();

        } catch (IOException | SaltpackException ex) {

            throw new AuroraException("Error while encrypting message for recipient: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getRecipient() {

        return recipient;
    }
}