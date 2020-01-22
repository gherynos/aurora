package co.naes.aurora.msg.key;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.msg.KeyMessage;
import net.nharyes.libsaltpack.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutKeyMessage extends KeyMessage {

    private String recipientIdentifier;

    private char[] password;

    public OutKeyMessage(AuroraSession session, String recipientIdentifier, boolean armored) throws AuroraException {

        try {

            this.recipientIdentifier = recipientIdentifier;

            // generate random password
            password = "aRandomPasswordToGenerate".toCharArray(); // TODO: fix

            // derive key from password
            byte[][][] symmetricKeys = {deriveKeyFromPassword(password)};
            byte[][] recipients = {};

            // pack key
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packString(session.getEmailAddress())
                    .packBinaryHeader(session.getPublicKey().length)
                    .writePayload(session.getPublicKey());
            packer.close();

            // signcrypt public key
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputParameters op = new OutputParameters(out);
            op.setArmored(armored);
            if (armored) {

                op.setApp(APP);
                op.setLettersInWords(15);
                op.setWordsInPhrase(5);
            }

            MessageWriter enc = new MessageWriter(op, session.getSignSecretKey(), recipients, symmetricKeys);
            enc.addBlock(packer.toByteArray(), true);

            out.flush();
            enc.destroy();

            ciphertext = out.toByteArray();

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while signcrypting key: " + ex.getMessage(), ex);
        }
    }

    public String getRecipientIdentifier() {

        return recipientIdentifier;
    }

    public char[] getPassword() {

        return password;
    }
}
