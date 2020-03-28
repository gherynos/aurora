package co.naes.aurora.parts;

public class Part {  // NOPMD

    private final PartId id;

    private final int total;

    private final long totalSize;

    private final byte[] data;

    public Part(PartId id, int total, long totalSize, byte[] data) {

        this.id = id;
        this.total = total;
        this.totalSize = totalSize;
        this.data = data.clone();
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

        return data.clone();
    }
}
