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

package co.naes.aurora.msg.in;

import co.naes.aurora.msg.InMessage;
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
