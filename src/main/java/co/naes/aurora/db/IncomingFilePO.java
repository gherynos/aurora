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

package co.naes.aurora.db;

import co.naes.aurora.AuroraException;
import co.naes.aurora.Identifier;

import java.sql.SQLException;
import java.sql.Timestamp;

public class IncomingFilePO {

    private final DBUtils db;

    private final String fileId;

    private String path;

    private final Identifier identifier;

    private final int totalParts;

    private Timestamp completed;

    public static IncomingFilePO get(DBUtils db, String fileId, Identifier identifier) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT * FROM INCOMING_FILES WHERE FILE_ID = ? AND IDENTIFIER = ?")) {

            st.setString(1, fileId);
            st.setString(2, identifier.serialise());
            var res = st.executeQuery();

            if (!res.next()) {

                return null;
            }

            return new IncomingFilePO(db, res.getString(1), res.getString(2),
                    new Identifier(res.getString(3)), res.getInt(4), res.getTimestamp(5));

        } catch (SQLException ex) {

            throw new AuroraException("Error while getting incoming file: " + ex.getMessage(), ex);
        }
    }

    public static void markFilesAsComplete(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.createStatement()) {

            st.execute("UPDATE INCOMING_FILES INC SET COMPLETED = CURRENT_TIMESTAMP() " +
                    "WHERE NOT EXISTS (SELECT SEQUENCE FROM PARTS_TO_RECEIVE PS WHERE INC.FILE_ID = PS.FILE_ID AND INC.IDENTIFIER = PS.IDENTIFIER LIMIT 1);");

        } catch (SQLException ex) {

            throw new AuroraException("Error while marking files as complete on the DB: " + ex.getMessage(), ex);
        }
    }

    private IncomingFilePO(DBUtils db, String fileId, String path, Identifier identifier, int totalParts, Timestamp completed) {

        this.db = db;
        this.fileId = fileId;
        this.path = path;
        this.identifier = identifier;
        this.totalParts = totalParts;
        this.completed = completed;
    }

    public IncomingFilePO(DBUtils db, String fileId, String path, Identifier identifier, int totalParts) {

        this(db, fileId, path, identifier, totalParts, null);
    }

    public void save() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("MERGE INTO INCOMING_FILES VALUES(?, ?, ?, ?, ?)")) {

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

    public void refreshCompleteStatus() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT COMPLETED FROM INCOMING_FILES WHERE FILE_ID = ? AND IDENTIFIER = ?")) {

            st.setString(1, fileId);
            st.setString(2, identifier.serialise());
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException("File not found in DB");
            }

            completed = res.getTimestamp(1);

        } catch (SQLException ex) {

            throw new AuroraException("Error while checking file in the DB: " + ex.getMessage(), ex);
        }
    }

    public String getFileId() {

        return fileId;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    public Identifier getIdentifier() {

        return identifier;
    }

    public int getTotalParts() {

        return totalParts;
    }

    public boolean isComplete() {

        return completed != null;
    }
}
