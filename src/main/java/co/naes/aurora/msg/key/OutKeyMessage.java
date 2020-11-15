/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
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

package co.naes.aurora.msg.key;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.ConstellationsHelper;
import co.naes.aurora.Identifier;
import co.naes.aurora.msg.KeyMessage;
import net.nharyes.libsaltpack.MessageWriter;
import net.nharyes.libsaltpack.OutputParameters;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutKeyMessage extends KeyMessage {

    private final Identifier recipientIdentifier;

    private final char[] password;

    public OutKeyMessage(AuroraSession session, Identifier recipientIdentifier, boolean armored) throws AuroraException {

        super();

        try {

            this.recipientIdentifier = recipientIdentifier;

            // generate random password
            password = ConstellationsHelper.getRandom(6);

            // derive key from password
            byte[][][] symmetricKeys = {deriveKeyFromPassword(password)};
            byte[][] recipients = {};


            try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {

                // pack key
                packer.packString(session.getPublicKeys().getIdentifier().serialise())
                        .packBinaryHeader(session.getPublicKeys().getPublicKey().length)
                        .writePayload(session.getPublicKeys().getPublicKey());

                // signcrypt public key
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputParameters op = new OutputParameters(out);
                op.setArmored(armored);
                if (armored) {

                    op.setApp(APP);
                    op.setLettersInWords(15);
                    op.setWordsInPhrase(5);
                }

                MessageWriter enc = new MessageWriter(op, session.getSignSecretKey(), recipients, symmetricKeys);
                enc.addBlock(packer.toByteArray(), true);

                out.flush();
                enc.destroy();

                ciphertext = out.toByteArray();
            }

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while signcrypting key: " + ex.getMessage(), ex);
        }
    }

    public Identifier getRecipientIdentifier() {

        return recipientIdentifier;
    }

    public char[] getPassword() {

        return password.clone();
    }
}
