/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gherynos.aurora.msg;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.AuroraSession;
import com.gherynos.aurora.PublicKeys;
import com.gherynos.aurora.msg.out.ConfOutMessage;
import com.gherynos.aurora.msg.out.PartOutMessage;
import com.gherynos.aurora.msg.out.PublicKeysOutMessage;
import com.gherynos.aurora.msg.out.StringOutMessage;
import com.gherynos.libsaltpack.MessageWriter;
import com.gherynos.libsaltpack.OutputParameters;
import com.gherynos.libsaltpack.SaltpackException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class OutMessage<T> extends CiphertextMessage {

    public static final Map<Class<? extends OutMessage<?>>, String> MAP;

    protected PublicKeys recipient;

    protected abstract void packData(MessageBufferPacker packer, T data) throws IOException;

    static {

        MAP = new HashMap<>();
        MAP.put(StringOutMessage.class, "Text");
        MAP.put(PartOutMessage.class, "Part");
        MAP.put(ConfOutMessage.class, "Conf");
        MAP.put(PublicKeysOutMessage.class, "PK");
    }

    public static String getIdentifier(Class<?> clazz) throws AuroraException {

        if (!MAP.containsKey(clazz)) {

            throw new AuroraException("Unknown class");
        }

        return MAP.get(clazz);
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public OutMessage(AuroraSession session, PublicKeys recipient, T data, boolean armored) throws AuroraException {

        super();

        this.recipient = recipient;

        // encrypt message
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            byte[][] recipients = {recipient.getPublicKey()};

            OutputParameters op = new OutputParameters(out);
            op.setArmored(armored);
            if (armored) {

                op.setApp(APP);
                op.setLettersInWords(999);
                op.setWordsInPhrase(1);
            }

            try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {

                // create binary data
                packData(packer, data);

                MessageWriter enc = new MessageWriter(op, session.getSecretKey(), recipients);

                byte[] buf = packer.toByteArray();
                int block = Math.min(buf.length, 1024 * 1024);
                int start = 0;
                for (int i = 0; i < buf.length / block; i++) {

                    enc.addBlock(buf, start, block, (buf.length - start) <= block);
                    start += block;
                }
                if (buf.length - start > 0) {

                    enc.addBlock(buf, start, buf.length - start, true);
                }

                out.flush();
                enc.destroy();

                ciphertext = out.toByteArray();
            }

        } catch (IOException | SaltpackException ex) {

            throw new AuroraException("Error while encrypting message for recipient: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getRecipient() {

        return recipient;
    }
}
