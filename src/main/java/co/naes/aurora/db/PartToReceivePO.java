package co.naes.aurora.db;

import co.naes.aurora.AuroraException;

import java.sql.SQLException;

public class PartToReceivePO {

    private final int sequenceNumber;

    private final String fileId;

    private final String emailAddress;

    public static void addAll(String fileId, String emailAddress, int totalParts) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("INSERT INTO PARTS_TO_RECEIVE VALUES(?, ?, ?)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.setString(3, emailAddress);
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

    public PartToReceivePO(int sequenceNumber, String fileId, String emailAddress) {

        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.emailAddress = emailAddress;
    }

    public void delete() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("DELETE FROM PARTS_TO_RECEIVE WHERE SEQUENCE = ? AND FILE_ID = ? AND EMAIL = ?")) {

            st.setInt(1, sequenceNumber);
            st.setString(2, fileId);
            st.setString(3, emailAddress);

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

    public String getEmailAddress() {

        return emailAddress;
    }
}
