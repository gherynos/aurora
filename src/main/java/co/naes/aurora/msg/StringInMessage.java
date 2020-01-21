package co.naes.aurora.msg;

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
