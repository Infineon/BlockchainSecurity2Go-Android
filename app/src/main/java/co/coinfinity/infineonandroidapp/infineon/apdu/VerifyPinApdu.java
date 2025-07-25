package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class VerifyPinApdu extends BaseCommandApdu {

    /**
     * Instruction byte for VERIFY PIN operation
     */
    private static final int INS_VERIFY_PIN = 0x44;

    /**
     * Constructs a VERIFY PIN command apdu.
     *
     * @param pin value used for initializing
     */
    public VerifyPinApdu(byte[] pin) {
        this.ins = INS_VERIFY_PIN;
        this.setData(pin);
    }

}
