package co.naes.aurora.ui;

public class OutgoingFile {

    private final String fileId;

    private final String recipient;

    private final int sent;

    private final int toSend;

    private final int total;

    public OutgoingFile(String fileId, String recipient, int sent, int toSend, int total) {

        this.fileId = fileId;
        this.recipient = recipient;
        this.sent = sent;
        this.toSend = toSend;
        this.total = total;
    }

    public Object[] asRow() {

        return new Object[]{fileId, recipient, total - (sent + toSend), toSend, total};
    }
}