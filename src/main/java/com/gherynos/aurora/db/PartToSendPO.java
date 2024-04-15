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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartToSendPO {

    public static final int COUNTER = 5;

    private final DBUtils db;

    private final int sequenceNumber;

    private final String fileId;

    private final Identifier identifier;

    private final boolean sentOnce;

    private final int counter;

    public static void addAll(DBUtils db, String fileId, Identifier identifier, int totalParts) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("INSERT INTO PARTS_TO_SEND VALUES(?, ?, ?, FALSE, ?)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.setString(3, identifier.serialise());
                st.setInt(4, COUNTER);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != totalParts) {

                throw new SQLException("Unable to insert all the parts");
            }

            conn.commit();

        } catch (SQLException ex) {  // NOPMD

            throw new AuroraException("Error while adding part to send to the DB: " + ex.getMessage(), ex);
        }
    }

    public static List<PartToSendPO> getNeverSent(DBUtils db, String fileId, Identifier identifier) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PARTS_TO_SEND WHERE FILE_ID = ? AND IDENTIFIER = ? AND SENT_ONCE = FALSE")) {

            List<PartToSendPO> out = new ArrayList<>();
            st.setString(1, fileId);
            st.setString(2, identifier.serialise());
            var res = st.executeQuery();
            while (res.next()) {

                out.add(new PartToSendPO(db, res.getInt(1), res.getString(2),
                        new Identifier(res.getString(3)), res.getBoolean(4), res.getInt(5)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public static void markAsSent(DBUtils db, List<Integer> sequenceNumbers, String fileId, Identifier identifier) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("UPDATE PARTS_TO_SEND SET SENT_ONCE = TRUE, COUNTER = ? WHERE SEQUENCE = ? AND FILE_ID = ? AND IDENTIFIER = ?")) {

            conn.setAutoCommit(false);

            for (Integer sequenceNumber : sequenceNumbers) {

                st.setInt(1, COUNTER);
                st.setInt(2, sequenceNumber);
                st.setString(3, fileId);
                st.setString(4, identifier.serialise());
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != sequenceNumbers.size()) {

                throw new SQLException("Unable to mark parts as sent");
            }

            conn.commit();

        } catch (SQLException ex) {  // NOPMD

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }

    public static void decreaseCounters(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.createStatement()) {

            st.execute("UPDATE PARTS_TO_SEND SET COUNTER = COUNTER - 1 WHERE SENT_ONCE = TRUE;");
            st.execute("UPDATE PARTS_TO_SEND SET SENT_ONCE = FALSE WHERE SENT_ONCE = TRUE AND COUNTER = 0;");

        } catch (SQLException ex) {

            throw new AuroraException("Error while decreasing counters on the DB: " + ex.getMessage(), ex);
        }
    }

    public PartToSendPO(DBUtils db, int sequenceNumber, String fileId, Identifier identifier, boolean sentOnce, int counter) {

        this.db = db;
        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.identifier = identifier;
        this.sentOnce = sentOnce;
        this.counter = counter;
    }

    public PartToSendPO(DBUtils db, int sequenceNumber, String fileId, Identifier identifier) {

        this.db = db;
        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.identifier = identifier;
        this.sentOnce = false;
        this.counter = COUNTER;
    }

    public void delete() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("DELETE FROM PARTS_TO_SEND WHERE SEQUENCE = ? AND FILE_ID = ? AND IDENTIFIER = ?")) {

            st.setInt(1, sequenceNumber);
            st.setString(2, fileId);
            st.setString(3, identifier.serialise());

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

    public Identifier getIdentifier() {

        return identifier;
    }

    public boolean wasSentOnce() {

        return sentOnce;
    }

    public int getCounter() {

        return counter;
    }
}
