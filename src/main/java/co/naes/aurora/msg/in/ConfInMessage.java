package co.naes.aurora.msg.in;

import co.naes.aurora.msg.InMessage;
import co.naes.aurora.parts.PartId;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class ConfInMessage extends InMessage<PartId> {

    @Override
    protected PartId unpackData(MessageUnpacker unpacker) throws IOException {

        String fileId = unpacker.unpackString();
        int sequenceNumber = unpacker.unpackInt();

        return new PartId(fileId, sequenceNumber);
    }

    public ConfInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
