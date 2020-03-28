package co.naes.aurora.parts;

public class PartId {

    private final String fileId;

    private final int sequenceNumber;

    public PartId(String fileId, int sequenceNumber) {

        this.fileId = fileId;
        this.sequenceNumber = sequenceNumber;
    }

    public String getFileId() {

        return fileId;
    }

    public int getSequenceNumber() {

        return sequenceNumber;
    }
}
