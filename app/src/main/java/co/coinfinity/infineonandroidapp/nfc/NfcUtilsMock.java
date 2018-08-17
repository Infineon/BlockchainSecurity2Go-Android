package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.ByteWriter;
import co.coinfinity.infineonandroidapp.common.Utils;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import static android.support.constraint.Constraints.TAG;
import static org.web3j.utils.Assertions.verifyPrecondition;

public class NfcUtilsMock {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    static Credentials credentials = Credentials.create(new BigInteger("79166386603517236976726811532830064984355265773618493467297037703400211058279").toString(16),
            new BigInteger("1151270011825183223805235897419104860957743818568039421199126264923822213203842512949016734652068699212645732466302596308750268989266518249615792739627611").toString(16));

    public static String getPublicKey(IsoDep isoDep, int parameter) throws IOException {
        byte[] response = credentials.getEcKeyPair().getPublicKey().toByteArray();
        String hex = Utils.bytesToHex(response);
        return hex;
    }

    public static String signTransaction(Tag tag, int parameter, byte[] data) throws IOException {
//        final BigDecimal value = Convert.toWei("0.0003", Convert.Unit.ETHER);
//        final BigDecimal gasPrice = Convert.toWei("1", Convert.Unit.GWEI);
//        final BigDecimal gasLimit = Convert.toWei("121000", Convert.Unit.WEI);
//
//        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));
//        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
//                getNextNonce(web3, credentials.getAddress()), gasPrice.toBigInteger(), gasLimit.toBigInteger(), "0xe09eD054044763E03e0e59460F773F69DB9A333A", value.toBigInteger());
//
//        return Utils.bytesToHex(TransactionEncoder.signMessage(rawTransaction, credentials));
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

    private static BigInteger recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        verifyPrecondition(recId >= 0, "recId must be positive");
        verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        verifyPrecondition(message != null, "message cannot be null");

        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion
        //        routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        //        using the conversion routine specified in Section 2.3.4. If this conversion
        //        routine outputs "invalid", then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, message);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);

        byte[] qBytes = q.getEncoded(false);
        // We remove the prefix
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }
}
