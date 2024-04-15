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
import com.gherynos.libsaltpack.Constants;
import com.gherynos.libsaltpack.SaltpackException;
import com.gherynos.libsaltpack.Utils;

public abstract class KeyMessage extends CiphertextMessage {

    protected static final byte[] SALT = "84e=tz+>4AH8L9A4".getBytes();

    private static final byte[] IDENTIFIER = "AuroraTempKey".getBytes();

    protected KeyMessage() {

        super();
    }

    protected byte[][] deriveKeyFromPassword(char[] password) throws AuroraException {

        try {

            // derive key from password
            byte[] bin = Utils.deriveKeyFromPassword(Constants.CRYPTO_BOX_SECRETKEYBYTES, password, SALT,
                    Constants.CRYPTO_PWHASH_OPSLIMIT_MODERATE, Constants.CRYPTO_PWHASH_MEMLIMIT_MODERATE);

            return new byte[][]{IDENTIFIER, bin};

        } catch (SaltpackException ex) {

            throw new AuroraException("Error while deriving key from password: " + ex.getMessage(), ex);
        }
    }
}
