/*
 * Copyright (C) 2020  Luca Zanconato (<github.com/gherynos>)
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

package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

import java.util.Properties;

public class AuroraSession {

    private final DBUtils db;

    private final byte[] secretKey;

    private final byte[] secretSignKey;

    private final PublicKeys publicKeys;

    protected AuroraSession(DBUtils db) throws AuroraException {

        this.db = db;

        try {

            Properties p = db.getProperties();

            // load existing keys from DB
            byte[] publicKey;
            byte[] publicSignKey;
            if (p.containsKey(DBUtils.SESSION_PUBLIC_KEY)) {

                publicKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_PUBLIC_KEY), Constants.ALPHABET_BASE62);
                secretKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SECRET_KEY), Constants.ALPHABET_BASE62);
                publicSignKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SIGN_PUBLIC_KEY), Constants.ALPHABET_BASE62);
                secretSignKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SIGN_SECRET_KEY), Constants.ALPHABET_BASE62);

            } else {

                // generate encryption keys
                publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
                secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
                Utils.generateKeypair(publicKey, secretKey);

                // generate signature keys
                secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
                publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];
                Utils.generateSignKeypair(publicSignKey, secretSignKey);

                // store keys
                p.setProperty(DBUtils.SESSION_PUBLIC_KEY, Utils.baseXencode(publicKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SECRET_KEY, Utils.baseXencode(secretKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SIGN_PUBLIC_KEY, Utils.baseXencode(publicSignKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SIGN_SECRET_KEY, Utils.baseXencode(secretSignKey, Constants.ALPHABET_BASE62));
                db.saveProperties();
            }

            publicKeys = new PublicKeys(publicKey, publicSignKey, new Identifier(p.getProperty(DBUtils.ACCOUNT_NAME),
                    p.getProperty(DBUtils.SESSION_EMAIL_ADDRESS)));

        } catch (SaltpackException ex) {

            throw new AuroraException("Unable to generate keys: " + ex.getMessage(), ex);
        }
    }

    public byte[] getSecretKey() {

        return secretKey.clone();
    }

    public byte[] getSignSecretKey() {

        return secretSignKey.clone();
    }

    public PublicKeys getPublicKeys() {

        return publicKeys;
    }

    public DBUtils getDBUtils() {

        return db;
    }
}
