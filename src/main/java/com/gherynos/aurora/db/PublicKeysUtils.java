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

package com.gherynos.aurora.db;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.Identifier;
import com.gherynos.aurora.PublicKeys;
import com.gherynos.libsaltpack.Constants;
import com.gherynos.libsaltpack.SaltpackException;
import com.gherynos.libsaltpack.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PublicKeysUtils {

    public static void store(DBUtils db, PublicKeys keys) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("MERGE INTO PUBLIC_KEYS KEY(IDENTIFIER) VALUES(?, ?, ?)")) {

            st.setString(1, keys.getIdentifier().serialise());
            st.setString(2, Utils.baseXencode(keys.getPublicKey(), Constants.ALPHABET_BASE62));
            st.setString(3, Utils.baseXencode(keys.getPublicSignKey(), Constants.ALPHABET_BASE62));

            st.execute();

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while storing keys to the DB: " + ex.getMessage(), ex);
        }
    }

    public static PublicKeys get(DBUtils db, Identifier identifier) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE IDENTIFIER = ?")) {

            st.setString(1, identifier.serialise());
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException(String.format("Key for '%s' not found.", identifier));
            }

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), new Identifier(id));

        } catch (SQLException | SaltpackException ex) {  // NOPMD

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public static PublicKeys get(DBUtils db, byte[] encryptionKey) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE ENCRYPTION = ?")) {

            st.setString(1, Utils.baseXencode(encryptionKey, Constants.ALPHABET_BASE62));
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException("Entry for encryption key provided not found.");
            }

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), new Identifier(id));

        } catch (SQLException | SaltpackException ex) {  // NOPMD

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public static List<Identifier> listIdentifiers(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.createStatement()) {

            List<Identifier> out = new ArrayList<>();
            var res = st.executeQuery("SELECT IDENTIFIER FROM PUBLIC_KEYS");
            while (res.next()) {

                out.add(new Identifier(res.getString(1)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys identifiers from the DB: " + ex.getMessage(), ex);
        }
    }

    private PublicKeysUtils() { }
}
