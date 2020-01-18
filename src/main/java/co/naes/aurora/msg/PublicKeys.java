package co.naes.aurora.msg;

public class PublicKeys {

    private byte[] publicKey;

    private byte[] publicSignKey;

    private String identifier;

    public PublicKeys(byte[] publicKey, String identifier) {

        this.publicKey = publicKey;
        this.identifier = identifier;
    }

    public PublicKeys(byte[] publicKey, byte[] publicSignKey, String identifier) {

        this.publicKey = publicKey;
        this.publicSignKey = publicSignKey;
        this.identifier = identifier;
    }

    public byte[] getPublicKey() {

        return publicKey;
    }

    public byte[] getPublicSignKey() {

        return publicSignKey;
    }

    public String getIdentifier() {

        return identifier;
    }
}
