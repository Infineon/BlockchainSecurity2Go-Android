package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class SelectApplicationApdu extends BaseCommandApdu {

    /**
     * Instruction byte for SELECT APPLICATION operation.
     */
    private static final int INS_SELECT_APPLICATION = 0xA4;

    private static final byte[] AID = {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x15, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01
    };

    /**
     * Constructs a SELECT APPLICATION command apdu.
     */
    public SelectApplicationApdu() {
        this.ins = INS_SELECT_APPLICATION;
        this.p1 = 0x04;
//        this.setData( Hex.decode("D2 76 00 00 04 15 02 00 01 00 00 00 01"));
        this.setData(AID);
    }

}
