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

public class IncomingFilePO {

    private final DBUtils db;

    private final String fileId;

    private final String path;

    private final String emailAddress;

    private final int totalParts;

    private boolean complete;

    public static IncomingFilePO get(DBUtils db, String fileId, String emailAddress) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT * FROM INCOMING_FILES WHERE FILE_ID = ? AND EMAIL = ?")) {

            st.setString(1, fileId);
            st.setString(2, emailAddress);
            var res = st.executeQuery();

            if (!res.next()) {

                return null;
            }

            return new IncomingFilePO(db, res.getString(1), res.getString(2),
                    res.getString(3), res.getInt(4), res.getBoolean(5));

        } catch (SQLException ex) {

            throw new AuroraException("Error while getting incoming file: " + ex.getMessage(), ex);
        }
    }

    public static void markFilesAsComplete(DBUtils db) throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.createStatement()) {

            st.execute("UPDATE INCOMING_FILES INC SET COMPLETE = TRUE WHERE NOT EXISTS (SELECT SEQUENCE FROM PARTS_TO_RECEIVE PS WHERE INC.FILE_ID = PS.FILE_ID AND INC.EMAIL = PS.EMAIL LIMIT 1);");

        } catch (SQLException ex) {

            throw new AuroraException("Error while marking files as complete on the DB: " + ex.getMessage(), ex);
        }
    }

    private IncomingFilePO(DBUtils db, String fileId, String path, String emailAddress, int totalParts, boolean complete) {

        this.db = db;
        this.fileId = fileId;
        this.path = path;
        this.emailAddress = emailAddress;
        this.totalParts = totalParts;
        this.complete = complete;
    }

    public IncomingFilePO(DBUtils db, String fileId, String path, String emailAddress, int totalParts) {

        this(db, fileId, path, emailAddress, totalParts, false);
    }

    public void save() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("INSERT INTO INCOMING_FILES VALUES(?, ?, ?, ?, ?)")) {

            st.setString(1, fileId);
            st.setString(2, path);
            st.setString(3, emailAddress);
            st.setInt(4, totalParts);
            st.setBoolean(5, complete);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public void refreshCompleteStatus() throws AuroraException {

        try (var conn = db.getConnection();
             var st = conn.prepareStatement("SELECT COMPLETE FROM INCOMING_FILES WHERE FILE_ID = ? AND EMAIL = ?")) {

            st.setString(1, fileId);
            st.setString(2, emailAddress);
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException("File not found in DB");
            }

            complete = res.getBoolean(1);

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

    public String getEmailAddress() {

        return emailAddress;
    }

    public int getTotalParts() {

        return totalParts;
    }

    public boolean isComplete() {

        return complete;
    }
}
