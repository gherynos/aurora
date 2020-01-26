package co.naes.aurora.msg.out;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.OutMessage;
import co.naes.aurora.parts.Part;
import org.msgpack.core.MessageBufferPacker;

import java.io.IOException;

public class PartOutMessage extends OutMessage<Part> {

    @Override
    protected void packData(MessageBufferPacker packer, Part part) throws IOException {

        packer.packString(part.getId().getFileId());
        packer.packInt(part.getId().getSequenceNumber());
        packer.packInt(part.getTotal());
        packer.packLong(part.getTotalSize());
        packer.packBinaryHeader(part.getData().length);
        packer.writePayload(part.getData());
    }

    public PartOutMessage(AuroraSession session, PublicKeys recipient, Part part, boolean armored) throws AuroraException {

        super(session, recipient, part, armored);
    }
}