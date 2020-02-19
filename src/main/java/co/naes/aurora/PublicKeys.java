package co.naes.aurora;

public class PublicKeys {

    private byte[] publicKey;

    private byte[] publicSignKey;

    private String emailAddress;

    public PublicKeys(byte[] publicKey, String emailAddress) {

        this.publicKey = publicKey;
        this.emailAddress = emailAddress;
    }

    public PublicKeys(byte[] publicKey, byte[] publicSignKey, String emailAddress) {

        this.publicKey = publicKey;
        this.publicSignKey = publicSignKey;
        this.emailAddress = emailAddress;
    }

    public byte[] getPublicKey() {

        return publicKey;
    }

    public byte[] getPublicSignKey() {

        return publicSignKey;
    }

    public String getEmailAddress() {

        return emailAddress;
    }

    @Override
    public String toString() {

        return emailAddress;
    }
}
