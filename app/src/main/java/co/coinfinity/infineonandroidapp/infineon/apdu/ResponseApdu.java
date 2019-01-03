package co.coinfinity.infineonandroidapp.infineon.apdu;

import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;

/**
 * Response APDU as received from the card.
 */
public class ResponseApdu {


    public static final int SW_SUCCESS = 0x9000;
    public static final int SW_KEY_WITH_IDX_NOT_AVAILABLE = 0x6A88;
    public static final int SW_SUCCESS_WITH_RESPONSE = 0x61;

    // According to ISO 7816-4 last two bytes are the "status words" (SW1 and SW2)
    /**
     * SW1 byte
     */
    protected int sw1 = 0x00;

    /**
     * SW2 byte
     */
    protected int sw2 = 0x00;

    /**
     * Data section of response APDU
     */
    protected byte[] data = new byte[0];

    /**
     * Raw byte representation of this APDU as received over NFC
     */
    protected byte[] apduBytes = new byte[0];


    /**
     * Constructor from raw bytes
     *
     * @param respApdu raw bytes as transceived from NFC card
     */
    public ResponseApdu(byte[] respApdu) {
        if (respApdu.length < 2) {
            throw new IllegalArgumentException("Illegal Response APDU. length < 2");
        }
        if (respApdu.length > 2) {
            data = new byte[respApdu.length - 2];
            System.arraycopy(respApdu, 0, data, 0, respApdu.length - 2);
        }
        sw1 = 0xFF & respApdu[respApdu.length - 2];
        sw2 = 0xFF & respApdu[respApdu.length - 1];
        apduBytes = respApdu;
    }

    /**
     * @return SW1 byte
     */
    public int getSW1() {
        return sw1;
    }

    /**
     * @return SW2 byte
     */
    public int getSW2() {
        return sw2;
    }

    /**
     * @return SW1 and SW2 interpreted as one integer
     */
    public int getSW1SW2() {
        return (sw1 << 8) | sw2;
    }

    /**
     * @return SW1 and SW2 interpreted as one integer
     */
    public String getSW1SW2HexString() {
        return bytesToHex(new byte[]{(byte) getSW1(), (byte) getSW2()});
    }

    /**
     * @return data part of the response APDU
     */
    public byte[] getData() {
        return data;
    }


    /**
     * @return data part of the response APDU (as hex string)
     */
    public String getDataAsHex() {
        return bytesToHex(data);
    }

    /**
     * Get raw byte representation of this APDU
     *
     * @return raw apdu bytes
     */
    public byte[] toBytes() {
        return apduBytes;
    }

    /**
     * @return true if the Status words indicated Success, false otherwise
     */
    public boolean isSuccess() {
        return getSW1SW2() == SW_SUCCESS || getSW1() == SW_SUCCESS_WITH_RESPONSE;
    }

    /**
     * @return the APDU's bytes as hexadecimal String
     */
    public String toHexString() {
        return bytesToHex(apduBytes);
    }
}