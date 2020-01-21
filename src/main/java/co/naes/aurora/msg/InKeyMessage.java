package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.PublicKeys;
import net.nharyes.libsaltpack.InputParameters;
import net.nharyes.libsaltpack.MessageReader;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InKeyMessage extends KeyMessage {

    public InKeyMessage(byte[] ciphertext) {

        this.ciphertext = ciphertext;
    }

    public PublicKeys getPublicKeys(char[] password) throws AuroraException {

        try {

            // derive key from password
            byte[][] key = deriveKeyFromPassword(password);

            // decrypt message
            ByteArrayInputStream in = new ByteArrayInputStream(ciphertext);
            InputParameters ip = new InputParameters(in);
            ip.setArmored(isArmored());

            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            MessageReader dec = new MessageReader(ip, new byte[]{}, key);
            while (dec.hasMoreBlocks())
                msg.writeBytes(dec.getBlock());
            byte[] publicSignKey = dec.getSender();
            dec.destroy();

            // unpack key
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(msg.toByteArray());

            String identifier = unpacker.unpackString();
            Value v = unpacker.unpackValue();
            byte[] publicKey = v.asBinaryValue().asByteArray();
            unpacker.close();

            return new PublicKeys(publicKey, publicSignKey, identifier);

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while decrypting key: " + ex.getMessage(), ex);
        }
    }
}
