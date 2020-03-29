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

package co.naes.aurora.msg;

import java.util.Arrays;

abstract class CiphertextMessage {  // NOPMD

    public static final String APP = "AURORA";
    public static final String ARMOR_BEGIN = String.format("BEGIN %s SALTPACK ENCRYPTED MESSAGE.", APP);
    public static final String ARMOR_END = String.format("END %s SALTPACK ENCRYPTED MESSAGE.", APP);

    protected byte[] ciphertext;

    protected CiphertextMessage() { }

    public boolean isArmored() {

        if (ciphertext == null || ciphertext.length < ARMOR_BEGIN.length()) {

            return false;
        }

        return Arrays.equals(ARMOR_BEGIN.getBytes(), 0, ARMOR_BEGIN.length(),
                ciphertext, 0, ARMOR_BEGIN.length());
    }

    public byte[] getCiphertext() {

        return ciphertext.clone();
    }
}
