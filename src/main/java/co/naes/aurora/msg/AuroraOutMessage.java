package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import net.nharyes.libsaltpack.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AuroraOutMessage extends AuroraMessage {

    private PublicKeys recipient;

    public AuroraOutMessage(AuroraSession session, PublicKeys recipient, String messageId, int sequenceNumber, int total, String data, boolean armored) throws AuroraException {

        construct(session, recipient, messageId, sequenceNumber, total, data, armored);
    }

    public AuroraOutMessage(AuroraSession session, PublicKeys recipient, String messageId, int sequenceNumber, int total, byte[] data, boolean armored) throws AuroraException {

        construct(session, recipient, messageId, sequenceNumber, total, data, armored);
    }

    private void construct(AuroraSession session, PublicKeys recipient, String messageId, int sequenceNumber, int total, Object data, boolean armored) throws AuroraException {

        this.recipient = recipient;
        this.messageId = messageId;
        this.sequenceNumber = sequenceNumber;
        this.total = total;

        // Encrypt message
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

            MessageWriter enc = new MessageWriter(op, session.getSecretKey(), recipients);
            enc.addBlock(packMessage(data), true);

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
