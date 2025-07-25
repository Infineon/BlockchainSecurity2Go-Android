package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class GenerateKeyFromSeedApdu extends BaseCommandApdu {

    /**
     * Instruction byte for GENERATE KEY FROM SEED operation.
     */
    private static final int INS_GENERATE_KEY_FROM_SEED = 0x20;

    /**
     * Constructs a GENERATE KEY FROM SEED command apdu.
     *
     * @param seedData seed to derive a keypair
     */
    public GenerateKeyFromSeedApdu(byte[] seedData) {
        this.ins = INS_GENERATE_KEY_FROM_SEED;
        this.setData(seedData);
    }

}
