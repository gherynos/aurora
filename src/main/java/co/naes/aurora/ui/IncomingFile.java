package co.naes.aurora.ui;

public class IncomingFile {

    private final String fileId;

    private final String sender;

    private final int missing;

    private final int total;

    public IncomingFile(String fileId, String sender, int missing, int total) {

        this.fileId = fileId;
        this.sender = sender;
        this.missing = missing;
        this.total = total;
    }

    public Object[] asRow() {

        return new Object[]{sender, fileId, total - missing, total};
    }
}
