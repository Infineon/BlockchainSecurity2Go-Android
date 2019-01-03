package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class SetPinApdu extends BaseCommandApdu {

    /**
     * Instruction byte for GET PUBKEY operation
     */
    private static final int INS_SET_PIN = 0x40;

    /**
     * Constructs a GET PUBKEY command apdu.
     *
     * @param pin value used for initializing
     */
    public SetPinApdu(byte[] pin) {
        this.ins = INS_SET_PIN;
        this.setData(pin);
        this.leIncluded = true;
    }

}
