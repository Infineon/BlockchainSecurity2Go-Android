package co.coinfinity.infineonandroidapp.infineon.exceptions;

/**
 * Represents errors received from the NFC card.
 */
public class NfcCardException extends Exception {

    private final int sw1sw2;
    private final String message;

    public NfcCardException(int SW1SW2, String message) {
        sw1sw2 = SW1SW2;
        this.message = message;
    }

    public int getSw1Sw2() {
        return sw1sw2;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{" +
                "sw1sw2=" + Integer.toHexString(sw1sw2) +
                ", message='" + message + '\'' +
                '}';
    }
}
