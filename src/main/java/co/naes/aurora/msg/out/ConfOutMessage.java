package co.naes.aurora.msg.out;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.OutMessage;
import co.naes.aurora.parts.PartId;
import org.msgpack.core.MessageBufferPacker;

import java.io.IOException;

public class ConfOutMessage extends OutMessage<PartId> {

    @Override
    protected void packData(MessageBufferPacker packer, PartId partId) throws IOException {

        packer.packString(partId.getFileId());
        packer.packInt(partId.getSequenceNumber());
    }

    public ConfOutMessage(AuroraSession session, PublicKeys recipient, PartId data, boolean armored) throws AuroraException {

        super(session, recipient, data, armored);
    }
}
