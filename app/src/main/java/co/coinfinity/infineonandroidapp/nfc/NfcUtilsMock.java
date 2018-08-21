package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.ByteWriter;
import co.coinfinity.infineonandroidapp.common.Utils;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

import static android.support.constraint.Constraints.TAG;

public class NfcUtilsMock {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    //    static Credentials credentials = Credentials.create(new BigInteger("79166386603517236976726811532830064984355265773618493467297037703400211058279").toString(16),
//            new BigInteger("1151270011825183223805235897419104860957743818568039421199126264923822213203842512949016734652068699212645732466302596308750268989266518249615792739627611").toString(16));
    static Credentials credentials = Credentials.create("e3894ff8a138ca87e02609ff8b9c18314b6d02144121cda9d33da12880f48d00",
            "504faa0283ad44177451f74b354f0b886d224c5ff8a091449458a3c32e538649d5d010c21c9fe5479008ee7432cf347c1b2c320be63acc60e7bfb01d3bc93667");

    public static String getPublicKey(IsoDep isoDep, int parameter) throws IOException {
        byte[] response = credentials.getEcKeyPair().getPublicKey().toByteArray();
        String hex = Utils.bytesToHex(response);

        Log.d(TAG, "PRIVATE KEY (hex): " + credentials.getEcKeyPair().getPrivateKey().toString(16));
        Log.d(TAG, "PUBLIC KEY (hex): " + credentials.getEcKeyPair().getPublicKey().toString(16));
        return hex;
    }

    public static String signTransaction(Tag tag, int parameter, byte[] data) throws IOException {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(credentials.getEcKeyPair().getPrivateKey(), CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(data);

        ECDSASignature sig = new ECDSASignature(components[0], components[1]).toCanonicalised();

        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);
        Log.d(TAG, "R BEFORE: " + Utils.bytesToHex(r));
        Log.d(TAG, "S BEFORE: " + Utils.bytesToHex(s));

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

        final byte[] bytes = writer.toBytes();
        return Utils.bytesToHex(bytes);
    }
}
