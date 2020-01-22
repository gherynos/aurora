package co.naes.aurora.msg.in;

import co.naes.aurora.msg.InMessage;
import co.naes.aurora.parts.Part;
import co.naes.aurora.parts.PartId;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;

public class PartInMessage extends InMessage<Part> {

    @Override
    protected Part unpackData(MessageUnpacker unpacker) throws IOException {

        String fileId = unpacker.unpackString();
        int sequenceNumber = unpacker.unpackInt();
        int total = unpacker.unpackInt();
        long totalSize = unpacker.unpackLong();
        Value v = unpacker.unpackValue();
        byte[] data = v.asBinaryValue().asByteArray();

        return new Part(new PartId(fileId, sequenceNumber), total, totalSize, data);
    }

    public PartInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
