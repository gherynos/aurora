package co.naes.aurora.msg.in;

import co.naes.aurora.msg.InMessage;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StringInMessage extends InMessage<String> {

    @Override
    protected String unpackData(MessageUnpacker unpacker) throws IOException {

        return unpacker.unpackString();
    }

    public StringInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
