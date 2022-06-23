/*
 * Copyright (C) 2020-2022  Luca Zanconato (<github.com/gherynos>)
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
import co.naes.aurora.Identifier;
import co.naes.aurora.Messenger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OutgoingFilePO {

    private final DBUtils db;

    private final String fileId;

    private final String path;

    private final Identifier identifier;

    private final int totalParts;

    private final Timestamp completed;

    public static List<OutgoingFilePO> getPending(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.createStatement()) {

            List<OutgoingFilePO> out = new ArrayList<>();
            var res = st.executeQuery("SELECT OF.* FROM OUTGOING_FILES OF WHERE (SELECT COUNT(SEQUENCE) FROM PARTS_TO_SEND PS WHERE PS.FILE_ID = OF.FILE_ID AND PS.IDENTIFIER = OF.IDENTIFIER) > 0;");
            while (res.next()) {

                out.add(new OutgoingFilePO(db, res.getString(1),
                        res.getString(2), new Identifier(res.getString(3)), res.getInt(4), res.getTimestamp(5)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public static void markFilesAsComplete(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("UPDATE OUTGOING_FILES OUT SET COMPLETED = CURRENT_TIMESTAMP(), PATH = REPLACE(PATH, ?, '') " +
                     "WHERE NOT EXISTS (SELECT SEQUENCE FROM PARTS_TO_SEND PS WHERE OUT.FILE_ID = PS.FILE_ID AND OUT.IDENTIFIER = PS.IDENTIFIER LIMIT 1);")) {

            st.setString(1, Messenger.TEMP_FILE_EXTENSION);
            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while marking files as complete on the DB: " + ex.getMessage(), ex);
        }
    }

    public OutgoingFilePO(DBUtils db, String fileId, String path, Identifier identifier, int totalParts, Timestamp completed) {

        this.db = db;
        this.fileId = fileId;
        this.path = path;
        this.identifier = identifier;
        this.totalParts = totalParts;
        this.completed = completed;
    }

    public OutgoingFilePO(DBUtils db, String fileId, String path, Identifier identifier, int totalParts) {

        this(db, fileId, path, identifier, totalParts, null);
    }

    public void save() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("INSERT INTO OUTGOING_FILES VALUES(?, ?, ?, ?, ?)")) {

            st.setString(1, fileId);
            st.setString(2, path);
            st.setString(3, identifier.serialise());
            st.setInt(4, totalParts);
            st.setTimestamp(5, completed);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public String getFileId() {

        return fileId;
    }

    public String getPath() {

        return path;
    }

    public Identifier getIdentifier() {

        return identifier;
    }

    public int getTotalParts() {

        return totalParts;
    }

    public Timestamp getCompleted() {

        return completed;
    }
}
