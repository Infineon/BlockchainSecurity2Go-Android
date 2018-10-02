package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Johannes Zweng on 02.10.18.
 */
public class GenerateSignatureApdu extends BaseCommandApdu {

    /**
     * Instruction byte for GENERATE SIGNATURE operation
     */
    private static final int INS_GENERATE_SIGNATURE = 0x18;


    /**
     * No hashing is done on card, data is already hashed externally
     * and transmitted as hash into the card.
     *
     * Currently no other mode documented. More modes may follow..
     */
    private static final int P2_DATA_ALREADY_PREHASHED = 0x00;


    /**
     * Constructs a GENERATE SIGNATURE apdu.
     *
     * @param keyIndex   key to use, 0x00 means default key
     * @param dataToSign data to be signed
     */
    public GenerateSignatureApdu(int keyIndex, byte[] dataToSign) {
        this.ins = INS_GENERATE_SIGNATURE;
        this.data = dataToSign;
        this.p1 = keyIndex;
        this.p2 = P2_DATA_ALREADY_PREHASHED;
        this.leIncluded = true;
    }

}
