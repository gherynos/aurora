package co.naes.aurora.db;

import co.naes.aurora.AuroraException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartToSendPO {

    private static final int COUNTER = 5;

    private final int sequenceNumber;

    private final String fileId;

    private final String emailAddress;

    private final boolean sentOnce;

    private final int counter;

    public static void addAll(String fileId, String emailAddress, int totalParts) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("INSERT INTO PARTS_TO_SEND VALUES(?, ?, ?, FALSE, ?)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.setString(3, emailAddress);
                st.setInt(4, COUNTER);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != totalParts) {

                throw new SQLException("Unable to insert all the parts");
            }

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while adding part to send to the DB: " + ex.getMessage(), ex);
        }
    }

    public static List<PartToSendPO> getNeverSent(String fileId, String emailAddress) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PARTS_TO_SEND WHERE FILE_ID = ? AND EMAIL = ? AND SENT_ONCE = FALSE")) {

            List<PartToSendPO> out = new ArrayList<>();
            st.setString(1, fileId);
            st.setString(2, emailAddress);
            var res = st.executeQuery();
            while (res.next()) {

                out.add(new PartToSendPO(res.getInt(1), res.getString(2),  // NOPMD
                        res.getString(3), res.getBoolean(4), res.getInt(5)));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public static void markAsSent(List<Integer> sequenceNumbers, String fileId, String emailAddress) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("UPDATE PARTS_TO_SEND SET SENT_ONCE = TRUE, COUNTER = ? WHERE SEQUENCE = ? AND FILE_ID = ? AND EMAIL = ?")) {

            conn.setAutoCommit(false);

            for (Integer sequenceNumber : sequenceNumbers) {

                st.setInt(1, COUNTER);
                st.setInt(2, sequenceNumber);
                st.setString(3, fileId);
                st.setString(4, emailAddress);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != sequenceNumbers.size()) {

                throw new SQLException("Unable to mark parts as sent");
            }

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }

    public static void decreaseCounters() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            st.execute("UPDATE PARTS_TO_SEND SET COUNTER = COUNTER - 1 WHERE SENT_ONCE = TRUE;");
            st.execute("UPDATE PARTS_TO_SEND SET SENT_ONCE = FALSE WHERE SENT_ONCE = TRUE AND COUNTER = 0;");

        } catch (SQLException ex) {

            throw new AuroraException("Error while decreasing counters on the DB: " + ex.getMessage(), ex);
        }
    }

    public PartToSendPO(int sequenceNumber, String fileId, String emailAddress, boolean sentOnce, int counter) {

        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.emailAddress = emailAddress;
        this.sentOnce = sentOnce;
        this.counter = counter;
    }

    public PartToSendPO(int sequenceNumber, String fileId, String emailAddress) {

        this.sequenceNumber = sequenceNumber;
        this.fileId = fileId;
        this.emailAddress = emailAddress;
        this.sentOnce = false;
        this.counter = COUNTER;
    }

    public void delete() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("DELETE FROM PARTS_TO_SEND WHERE SEQUENCE = ? AND FILE_ID = ? AND EMAIL = ?")) {

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

    public boolean wasSentOnce() {

        return sentOnce;
    }

    public int getCounter() {

        return counter;
    }
}
