package co.naes.aurora.msg;

import java.util.Arrays;

abstract class CiphertextMessage {  // NOPMD

    public static final String APP = "AURORA";
    public static final String ARMOR_BEGIN = String.format("BEGIN %s SALTPACK ENCRYPTED MESSAGE.", APP);
    public static final String ARMOR_END = String.format("END %s SALTPACK ENCRYPTED MESSAGE.", APP);

    protected byte[] ciphertext;

    protected CiphertextMessage() { }

    public boolean isArmored() {

        if (ciphertext == null || ciphertext.length < ARMOR_BEGIN.length()) {

            return false;
        }

        return Arrays.equals(ARMOR_BEGIN.getBytes(), 0, ARMOR_BEGIN.length(),
                ciphertext, 0, ARMOR_BEGIN.length());
    }

    public byte[] getCiphertext() {

        return ciphertext.clone();
    }
}
