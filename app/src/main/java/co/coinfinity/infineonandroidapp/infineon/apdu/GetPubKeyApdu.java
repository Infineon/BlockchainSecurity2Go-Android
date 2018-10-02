package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class GetPubKeyApdu extends BaseCommandApdu {

    /**
     * Instruction byte for GET PUBKEY operation
     */
    private static final int INS_GET_PUBKEY = 0x16;

    /**
     * Constructs a GET PUBKEY command apdu.
     *
     * @param keyIndex key id on card, 0x00 means default key
     */
    public GetPubKeyApdu(int keyIndex) {
        if (keyIndex > 0xFF) {
            throw new IllegalArgumentException("KeyIndex cannot be larger than 0xFF");
        }
        this.ins = INS_GET_PUBKEY;
        this.p1 = keyIndex;
        this.leIncluded = true;
    }

}
