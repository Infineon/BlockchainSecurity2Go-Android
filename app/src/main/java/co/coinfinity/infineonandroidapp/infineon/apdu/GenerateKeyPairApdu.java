package co.coinfinity.infineonandroidapp.infineon.apdu;

/**
 * @author Coinfinity, 2018
 */
public class GenerateKeyPairApdu extends BaseCommandApdu {

    /**
     * Instruction byte for GENERATE KEYPAIR operation.
     */
    private static final int INS_GENERATE_KEYPAIR = 0x02;

    /**
     * Curve index, identifying the Secp256k1 elliptic curve.
     */
    public static final int CURVE_INDEX_SECP256K1 = 0x00;

    /**
     * Constructs a GENERATE KEYPAIR command apdu.
     *
     * @param ellipticCurveIndex key id on card, 0x00 means default key
     */
    public GenerateKeyPairApdu(int ellipticCurveIndex) {
        if (ellipticCurveIndex > 0xFF) {
            throw new IllegalArgumentException("Curve index cannot be larger than 0xFF");
        }
        this.ins = INS_GENERATE_KEYPAIR;
        this.p1 = ellipticCurveIndex;
        this.leIncluded = true;
    }

}
