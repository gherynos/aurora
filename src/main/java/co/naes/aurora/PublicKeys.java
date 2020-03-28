package co.naes.aurora;

public class PublicKeys {

    private final byte[] publicKey;

    private byte[] publicSignKey;

    private final String emailAddress;

    public PublicKeys(byte[] publicKey, String emailAddress) {

        this.publicKey = publicKey.clone();
        this.emailAddress = emailAddress;
    }

    public PublicKeys(byte[] publicKey, byte[] publicSignKey, String emailAddress) {

        this.publicKey = publicKey.clone();
        this.publicSignKey = publicSignKey.clone();
        this.emailAddress = emailAddress;
    }

    public byte[] getPublicKey() {

        return publicKey.clone();
    }

    public byte[] getPublicSignKey() {

        return publicSignKey.clone();
    }

    public String getEmailAddress() {

        return emailAddress;
    }

    @Override
    public String toString() {

        return emailAddress;
    }
}
