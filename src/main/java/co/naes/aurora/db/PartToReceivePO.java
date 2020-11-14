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

package co.naes.aurora.db;

import co.naes.aurora.AuroraException;

import java.sql.SQLException;

public class PartToReceivePO {

    private final DBUtils db;

    private final int sequenceNumber;

    private final String fileId;

    private final String identifier;

    public static void addAll(DBUtils db, String fileId, String identifier, int totalParts) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("INSERT INTO PARTS_TO_RECEIVE VALUES(?, ?, ?)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.setString(3, identifier);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != totalParts) {

                throw new SQLException("Unable to insert all the parts");
            }

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while adding parts to receive to the DB: " + ex.getMessage(), ex);
        }
    }

    public PartToReceivePO(DBUtils db, int sequenceNumber, String fileId, String identifier) {

        this.db = db;
        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.identifier = identifier;
    }

    public void delete() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("DELETE FROM PARTS_TO_RECEIVE WHERE SEQUENCE = ? AND FILE_ID = ? AND IDENTIFIER = ?")) {

            st.setInt(1, sequenceNumber);
            st.setString(2, fileId);
            st.setString(3, identifier);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }

    public int getSequenceNumber() {

        return sequenceNumber;
    }

    public String getFileId() {

        return fileId;
    }

    public String getIdentifier() {

        return identifier;
    }
}
