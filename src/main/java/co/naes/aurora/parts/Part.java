package co.naes.aurora.parts;

public class Part {

    private int sequenceNumber;

    private int total;

    private long totalSize;

    private byte[] data;

    public Part(int sequenceNumber, int total, long totalSize, byte[] data) {

        this.sequenceNumber = sequenceNumber;
        this.total = total;
        this.totalSize = totalSize;
        this.data = data;
    }

    public int getSequenceNumber() {

        return sequenceNumber;
    }

    public int getTotal() {

        return total;
    }

    public long getTotalSize() {

        return totalSize;
    }

    public byte[] getData() {

        return data;
    }
}
