package co.coinfinity.infineonandroidapp.ethereum.utils;

import co.coinfinity.infineonandroidapp.utils.ByteWriter;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionSigner {

    static final BigInteger GAS_PRICE = Convert.toWei("50", Convert.Unit.GWEI).toBigInteger();
    static final BigInteger GAS_LIMIT = new BigInteger("600000");
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
    //put in the private key (64 chars long) which you can export via MetaMask
    static Credentials credentials = Credentials.create("000000000000000000000000000000000000000000");

    public static byte[] signTransaction(byte[] data) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(TransactionSigner.credentials.getEcKeyPair().getPrivateKey(), CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(data);

        ECDSASignature sig = new ECDSASignature(components[0], components[1]).toCanonicalised();

        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        // Write DER encoding of signature
        ByteWriter writer = new ByteWriter(1024);
        // Write tag
        writer.put((byte) 0x30);
        // Write total length

        int totalLength = 2 + r.length + 2 + s.length;
        if (totalLength > 127) {
            // We assume that the total length never goes beyond a 1-byte
            // representation
            throw new RuntimeException("Unsupported signature length: " + totalLength);
        }
        writer.put((byte) (totalLength & 0xFF));
        // Write type
        writer.put((byte) 0x02);
        // We assume that the length never goes beyond a 1-byte representation
        writer.put((byte) (r.length & 0xFF));
        // Write bytes
        writer.putBytes(r);
        // Write type
        writer.put((byte) 0x02);
        // We assume that the length never goes beyond a 1-byte representation
        writer.put((byte) (s.length & 0xFF));
        // Write bytes
        writer.putBytes(s);

        return writer.toBytes();
    }
}
