package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class SelectApplicationApdu extends BaseCommandApdu {

    /**
     * Instruction byte for SELECT APPLICATION operation.
     */
    private static final int INS_SELECT_APPLICATION = 0xA4;

    /**
     * Constructs a SELECT APPLICATION command apdu.
     */
    public SelectApplicationApdu(byte[] aid) {
        this.ins = INS_SELECT_APPLICATION;
        this.p1 = 0x04;
        this.setData(aid);
    }

}
