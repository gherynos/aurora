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
import co.naes.aurora.ui.IncomingFile;
import co.naes.aurora.ui.OutgoingFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class StatusUtils {

    public static List<IncomingFile> getIncomingFiles() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            var res = st.executeQuery("SELECT INC.FILE_ID, INC.EMAIL, INC.TOTAL_PARTS, COUNT(P.SEQUENCE) FROM INCOMING_FILES INC, PARTS_TO_RECEIVE P WHERE P.FILE_ID = INC.FILE_ID GROUP BY P.FILE_ID");

            List<IncomingFile> out = new ArrayList<>();
            while (res.next()) {

                out.add(new IncomingFile(res.getString(1),  // NOPMD
                        res.getString(2), res.getInt(4), res.getInt(3)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading incoming files: " + ex.getMessage(), ex);
        }
    }

    public static List<OutgoingFile> getOutgoingFiles() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            var res = st.executeQuery("SELECT OF.FILE_ID, OF.EMAIL, OF.TOTAL_PARTS, (SELECT COUNT(SEQUENCE) FROM PARTS_TO_SEND WHERE FILE_ID=OF.FILE_ID AND SENT_ONCE=TRUE GROUP BY FILE_ID) AS SENT, (SELECT COUNT(SEQUENCE) FROM PARTS_TO_SEND WHERE FILE_ID=OF.FILE_ID AND SENT_ONCE=FALSE GROUP BY FILE_ID) AS TO_SEND FROM OUTGOING_FILES OF");

            List<OutgoingFile> out = new ArrayList<>();
            while (res.next()) {

                if (res.getObject(4) != null || res.getObject(5) != null) {

                    out.add(new OutgoingFile(res.getString(1), res.getString(2),  // NOPMD
                            res.getInt(4), res.getInt(5), res.getInt(3)));
                }
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading outgoing files: " + ex.getMessage(), ex);
        }
    }

    private StatusUtils() { }
}
