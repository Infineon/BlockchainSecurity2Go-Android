package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class UnlockPinApdu extends BaseCommandApdu {

    /**
     * Instruction byte for UNLOCK PIN operation
     */
    private static final int INS_UNLOCK_PIN = 0x46;

    /**
     * Constructs a UNLOCK PIN command apdu.
     *
     * @param puk value used for unlock
     */
    public UnlockPinApdu(byte[] puk) {
        this.ins = INS_UNLOCK_PIN;
        this.setData(puk);
    }

}
