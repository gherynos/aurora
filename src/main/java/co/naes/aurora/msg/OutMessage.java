package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.out.ConfOutMessage;
import co.naes.aurora.msg.out.PartOutMessage;
import co.naes.aurora.msg.out.StringOutMessage;
import net.nharyes.libsaltpack.MessageWriter;
import net.nharyes.libsaltpack.OutputParameters;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class OutMessage<T> extends CiphertextMessage {

    public static final Map<Class<? extends OutMessage<?>>, String> MAP;

    protected PublicKeys recipient;

    protected abstract void packData(MessageBufferPacker packer, T data) throws IOException;

    static {

        MAP = new HashMap<>();
        MAP.put(StringOutMessage.class, "Text");
        MAP.put(PartOutMessage.class, "Part");
        MAP.put(ConfOutMessage.class, "Conf");
    }

    public static String getIdentifier(Class<?> clazz) throws AuroraException {

        if (!MAP.containsKey(clazz)) {

            throw new AuroraException("Unknown class");
        }

        return MAP.get(clazz);
    }

    public OutMessage(AuroraSession session, PublicKeys recipient, T data, boolean armored) throws AuroraException {

        super();

        this.recipient = recipient;

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


            try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {

                // create binary data
                packData(packer, data);

                MessageWriter enc = new MessageWriter(op, session.getSecretKey(), recipients);

                byte[] buf = packer.toByteArray();
                int block = Math.min(buf.length, 1024 * 1024);
                int start = 0;
                for (int i = 0; i < buf.length / block; i++) {

                    enc.addBlock(buf, start, block, (buf.length - start) <= block);
                    start += block;
                }
                if (buf.length - start > 0) {

                    enc.addBlock(buf, start, buf.length - start, true);
                }

                out.flush();
                enc.destroy();

                ciphertext = out.toByteArray();
            }

        } catch (IOException | SaltpackException ex) {

            throw new AuroraException("Error while encrypting message for recipient: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getRecipient() {

        return recipient;
    }
}
