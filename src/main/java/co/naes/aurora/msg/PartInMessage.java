package co.naes.aurora.msg;

import co.naes.aurora.parts.Part;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;

public class PartInMessage extends InMessage<Part> {

    @Override
    protected Part unpackData(MessageUnpacker unpacker) throws IOException {

        int sequenceNumber = unpacker.unpackInt();
        int total = unpacker.unpackInt();
        long totalSize = unpacker.unpackLong();
        Value v = unpacker.unpackValue();
        byte[] data = v.asBinaryValue().asByteArray();

        return new Part(sequenceNumber, total, totalSize, data);
    }

    public PartInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
