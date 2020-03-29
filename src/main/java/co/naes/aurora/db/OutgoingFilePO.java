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
import java.util.ArrayList;
import java.util.List;

public class OutgoingFilePO {

    private final String fileId;

    private final String path;

    private final String emailAddress;

    private final int totalParts;

    public static List<OutgoingFilePO> getPending() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            List<OutgoingFilePO> out = new ArrayList<>();
            var res = st.executeQuery("SELECT OF.* FROM OUTGOING_FILES OF WHERE (SELECT COUNT(SEQUENCE) FROM PARTS_TO_SEND PS WHERE PS.FILE_ID = OF.FILE_ID AND PS.EMAIL = OF.EMAIL) > 0;");
            while (res.next()) {

                out.add(new OutgoingFilePO(res.getString(1),  // NOPMD
                        res.getString(2), res.getString(3), res.getInt(4)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public OutgoingFilePO(String fileId, String path, String emailAddress, int totalParts) {

        this.fileId = fileId;
        this.path = path;
        this.emailAddress = emailAddress;
        this.totalParts = totalParts;
    }

    public void save() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("INSERT INTO OUTGOING_FILES VALUES(?, ?, ?, ?)")) {

            st.setString(1, fileId);
            st.setString(2, path);
            st.setString(3, emailAddress);
            st.setInt(4, totalParts);

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

    public String getEmailAddress() {

        return emailAddress;
    }

    public int getTotalParts() {

        return totalParts;
    }
}
