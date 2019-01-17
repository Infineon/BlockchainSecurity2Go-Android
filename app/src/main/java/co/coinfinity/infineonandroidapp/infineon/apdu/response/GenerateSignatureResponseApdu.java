package co.coinfinity.infineonandroidapp.infineon.apdu.response;

import java.math.BigInteger;

/**
 * Response APDU of GENERATE SIGNATURE as received from the card.
 */
public class GenerateSignatureResponseApdu extends ResponseApdu {

    /**
     * Global signature counter section of response APDU
     */
    protected byte[] globalSigCounter = new byte[4];

    /**
     * Signature counter section of response APDU
     */
    protected byte[] sigCounter = new byte[4];

    /**
     * Signature section of response APDU
     */
    protected byte[] signature;

    /**
     * Constructor from raw bytes
     *
     * @param respApdu raw bytes as transceived from NFC card
     */
    public GenerateSignatureResponseApdu(byte[] respApdu) {
        super(respApdu);
        signature = new byte[respApdu.length - globalSigCounter.length - sigCounter.length];
        for (int i = 0; i < respApdu.length; i++) {
            if (i < globalSigCounter.length) {
                globalSigCounter[i] = respApdu[i];
            } else if (i < globalSigCounter.length + sigCounter.length) {
                sigCounter[i - globalSigCounter.length] = respApdu[i];
            } else if (i < globalSigCounter.length + sigCounter.length + signature.length) {
                signature[i - globalSigCounter.length - sigCounter.length] = respApdu[i];
            }
        }
    }

    public byte[] getGlobalSigCounter() {
        return globalSigCounter;
    }

    public int getGlobalSigCounterAsInteger() {
        return new BigInteger(globalSigCounter).intValue();
    }

    public byte[] getSigCounter() {
        return sigCounter;
    }

    public int getSigCounterAsInteger() {
        return new BigInteger(sigCounter).intValue();
    }

    public byte[] getSignature() {
        return signature;
    }
}
