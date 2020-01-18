package co.naes.aurora.msg;

abstract class CiphertextMessage {

    public static final String APP = "AURORA";
    public static final String ARMOR_BEGIN = String.format("BEGIN %s SALTPACK ENCRYPTED MESSAGE.", APP);
    public static final String ARMOR_END = String.format("END %s SALTPACK ENCRYPTED MESSAGE.", APP);

    protected byte[] ciphertext;

    public boolean isArmored() {

        // TODO: improve

        if (ciphertext == null || ciphertext.length < ARMOR_BEGIN.length())
            return false;

        byte[] ab = ARMOR_BEGIN.getBytes();

        for (short i = 0; i < ab.length; i++) {

            if (ciphertext[i] != ab[i])
                return false;
        }

        return true;
    }

    public byte[] getCiphertext() {

        return ciphertext;
    }
}
