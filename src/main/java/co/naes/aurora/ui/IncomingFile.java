package co.naes.aurora.ui;

public class IncomingFile {

    private String fileId;

    private String sender;

    private int missing;

    private int total;

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
