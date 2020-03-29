package co.naes.aurora.db;

import co.naes.aurora.AuroraException;

import java.sql.SQLException;

public class IncomingFilePO {

    private String fileId;

    private String path;

    private String emailAddress;

    private int totalParts;

    private boolean complete;

    public static IncomingFilePO get(String fileId, String emailAddress) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("SELECT * FROM INCOMING_FILES WHERE FILE_ID = ? AND EMAIL = ?")) {

            st.setString(1, fileId);
            st.setString(2, emailAddress);
            var res = st.executeQuery();

            if (!res.next()) {

                return null;
            }

            return new IncomingFilePO(res.getString(1), res.getString(2),
                    res.getString(3), res.getInt(4), res.getBoolean(5));

        } catch (SQLException ex) {

            throw new AuroraException("Error while getting incoming file: " + ex.getMessage(), ex);
        }
    }

    public static void markFilesAsComplete() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            st.execute("UPDATE INCOMING_FILES INC SET COMPLETE = TRUE WHERE NOT EXISTS (SELECT SEQUENCE FROM PARTS_TO_RECEIVE PS WHERE INC.FILE_ID = PS.FILE_ID AND INC.EMAIL = PS.EMAIL LIMIT 1);");

        } catch (SQLException ex) {

            throw new AuroraException("Error while marking files as complete on the DB: " + ex.getMessage(), ex);
        }
    }

    private IncomingFilePO(String fileId, String path, String emailAddress, int totalParts, boolean complete) {

        this.fileId = fileId;
        this.path = path;
        this.emailAddress = emailAddress;
        this.totalParts = totalParts;
        this.complete = complete;
    }

    public IncomingFilePO(String fileId, String path, String emailAddress, int totalParts) {

        this(fileId, path, emailAddress, totalParts, false);
    }

    public void save() throws AuroraException {

        try (var conn = DBUtils.getConnection();
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

        try (var conn = DBUtils.getConnection();
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
