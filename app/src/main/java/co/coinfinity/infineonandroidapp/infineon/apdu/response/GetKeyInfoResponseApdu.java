package co.coinfinity.infineonandroidapp.infineon.apdu.response;

import java.math.BigInteger;

import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;

/**
 * Response APDU of GET KEY INFO as received from the card.
 */
public class GetKeyInfoResponseApdu extends ResponseApdu {

    /**
     * Global signature counter section of response APDU
     */
    protected byte[] globalSigCounter = new byte[4];

    /**
     * Signature counter section of response APDU
     */
    protected byte[] sigCounter = new byte[4];

    /**
     * Public key section of response APDU
     */
    protected byte[] publicKey = new byte[65];

    /**
     * Constructor from raw bytes
     *
     * @param respApdu raw bytes as transceived from NFC card
     */
    public GetKeyInfoResponseApdu(byte[] respApdu) {
        super(respApdu);
        for (int i = 0; i < respApdu.length; i++) {
            if (i < globalSigCounter.length) {
                globalSigCounter[i] = respApdu[i];
            } else if (i < globalSigCounter.length + sigCounter.length) {
                sigCounter[i - globalSigCounter.length] = respApdu[i];
            } else if (i < globalSigCounter.length + sigCounter.length + publicKey.length) {
                publicKey[i - globalSigCounter.length - sigCounter.length] = respApdu[i];
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

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyInHex() {
        return bytesToHex(publicKey);
    }

    public String getPublicKeyInHexWithoutPrefix() {
        return bytesToHex(publicKey).substring(2);
    }
}
