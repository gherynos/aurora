package co.naes.aurora.parts;

public class Part {

    private PartId id;

    private int total;

    private long totalSize;

    private byte[] data;

    public Part(PartId id, int total, long totalSize, byte[] data) {

        this.id = id;
        this.total = total;
        this.totalSize = totalSize;
        this.data = data;
    }

    public PartId getId() {

        return id;
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
