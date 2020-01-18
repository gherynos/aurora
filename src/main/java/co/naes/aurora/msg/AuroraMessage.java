package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;

abstract class AuroraMessage extends CiphertextMessage {

    protected final short TYPE_TEXT = 1;
    protected final short TYPE_BINARY = 2;

    protected String messageId;
    protected short messageType;
    protected Integer sequenceNumber;
    protected Integer total;

    protected byte[] packMessage(Object data) throws AuroraException {

        if (messageId == null || messageId.isEmpty())
            throw new AuroraException("Please provide messageId");

        if (sequenceNumber == null)
            throw new AuroraException("Please provide sequenceNumber");

        if (total == null)
            throw new AuroraException("Please provide total");

        if (data instanceof String)
            messageType = TYPE_TEXT;
        else if (data instanceof byte[])
            messageType = TYPE_BINARY;
        else
            throw new AuroraException("Wrong data type");

        try {

            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packString(messageId)
                    .packShort(messageType)
                    .packInt(sequenceNumber)
                    .packInt(total);

            // pack data
            if (messageType == TYPE_TEXT) {

                assert data instanceof String;
                packer.packString((String) data);

            } else if (messageType == TYPE_BINARY) {

                assert data instanceof byte[];
                byte[] bData = (byte[]) data;
                packer.packBinaryHeader(bData.length);
                packer.writePayload(bData);
            }

            packer.close();

            return packer.toByteArray();

        } catch (IOException ex) {

            throw new AuroraException("Unable to pack message: " + ex.getMessage(), ex);
        }
    }

    protected Object unpackMessage(byte[] packed) throws AuroraException {

        try {

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packed);

            messageId = unpacker.unpackString();
            messageType = unpacker.unpackShort();
            sequenceNumber = unpacker.unpackInt();
            total = unpacker.unpackInt();

            if (messageType == TYPE_TEXT) {

                String data = unpacker.unpackString();
                unpacker.close();
                return data;

            } else if (messageType == TYPE_BINARY) {

                Value v = unpacker.unpackValue();
                byte[] data = v.asBinaryValue().asByteArray();
                unpacker.close();
                return data;

            } else {

                unpacker.close();
                throw new AuroraException("Wrong data type");
            }

        } catch (IOException ex) {

            throw new AuroraException("Unable to unpack message: " + ex.getMessage(), ex);
        }
    }
}
