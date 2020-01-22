package co.naes.aurora.msg.out;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.OutMessage;
import org.msgpack.core.MessageBufferPacker;

import java.io.IOException;

public class StringOutMessage extends OutMessage<String> {

    @Override
    protected void packData(MessageBufferPacker packer, String data) throws IOException {

        packer.packString(data);
    }

    public StringOutMessage(AuroraSession session, PublicKeys recipient, String data, boolean armored) throws AuroraException {

        super(session, recipient, data, armored);
    }
}
