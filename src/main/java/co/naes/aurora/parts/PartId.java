package co.naes.aurora.parts;

public class PartId {

    private String fileId;

    private int sequenceNumber;

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
