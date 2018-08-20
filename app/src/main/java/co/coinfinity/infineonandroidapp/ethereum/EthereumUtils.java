package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.Utils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Bytes;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static android.support.constraint.Constraints.TAG;
import static co.coinfinity.infineonandroidapp.nfc.NfcUtilsMock.CURVE;
import static org.web3j.crypto.TransactionEncoder.encode;
import static org.web3j.utils.Assertions.verifyPrecondition;

public class EthereumUtils {

    public static EthBalanceBean getBalance(String ethAddress) {
        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));

        BigInteger wei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.LATEST);
        BigDecimal ether = Convert.fromWei(wei.toString(), Convert.Unit.ETHER);
        Log.d("WEB3J", ether + " Ether");
        Log.d("WEB3J", wei + " Wei");

        BigInteger unconfirmedWei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.PENDING);
        BigDecimal unconfirmedEther = Convert.fromWei(unconfirmedWei.toString(), Convert.Unit.ETHER);
        Log.d("WEB3J", unconfirmedWei + " Ether unconfirmed");
        Log.d("WEB3J", unconfirmedEther + " Wei unconfirmed");

        return new EthBalanceBean(wei, ether, unconfirmedWei, unconfirmedEther);
    }

    private static BigInteger getBalanceFromApi(Web3j web3, String ethAddress, DefaultBlockParameterName defaultBlockParameterName) {
        BigInteger wei = null;
        // send synchronous requests to get balance
        try {
            EthGetBalance ethGetBalance = web3
                    .ethGetBalance(ethAddress, defaultBlockParameterName)
                    .send();

            if (ethGetBalance != null) {
                wei = ethGetBalance.getBalance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wei;
    }

    private static final String SECP256K1 = "secp256k1";

    public static void sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String from, String to, BigInteger value, Tag tagFromIntent, String publicKey) {

        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));
//        Web3j web3 = Web3jFactory.build(new HttpService("https://mainnet.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                getNextNonce(web3, from), gasPrice, gasLimit, to, value);

        //SIGN transaction
        String signedTransaction = null;
        String hexValue = null;
        try {
            byte[] encodedTransaction = encode(rawTransaction);
            final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
            //TODO change mock here
//            signedTransaction = NfcUtilsMock.signTransaction(tagFromIntent, 0x01, hashedTransaction);
            signedTransaction = NfcUtils.signTransaction(tagFromIntent, 0x01, hashedTransaction);
            Log.d(TAG, "signedTransaction: " + signedTransaction);
            assert signedTransaction != null;
            final byte[] signatureData = Utils.hexStringToByteArray(signedTransaction);

            final byte[] r = Bytes.trimLeadingZeroes(extractR(signatureData));
            final byte[] s = Bytes.trimLeadingZeroes(extractS(signatureData));
            Log.d(TAG, "r: " + Utils.bytesToHex(r));
            Log.d(TAG, "s: " + Utils.bytesToHex(s));

            byte v = getV(publicKey, hashedTransaction, r, s);
            Log.d(TAG, "v: " + v);

//            byte vNeu = (byte) (v + (ChainId.ROPSTEN << 1) + 8);
            Sign.SignatureData signature = new Sign.SignatureData(v, r, s);
//            Sign.SignatureData signature = new Sign.SignatureData(Integer.valueOf(3 * 2 + 35 + Byte.valueOf(v).intValue()).byteValue(), r, s);

            Class c = TransactionEncoder.class;
            Object obj = c.newInstance();
            Method m = c.getDeclaredMethod("encode", RawTransaction.class, Sign.SignatureData.class);
            m.setAccessible(true);

            final byte[] invoke = (byte[]) m.invoke(obj, rawTransaction, signature);
            hexValue = Numeric.toHexString(invoke);

        } catch (Exception e) {
            Log.e(TAG, "exception while signing", e);
        }

        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "exception while sending transaction", e);
        }
        assert ethSendTransaction != null;
        String transactionHash = ethSendTransaction.getTransactionHash();
        Log.d(TAG, "TransactionHash: " + transactionHash);
        Log.d(TAG, "TransactionResult: " + ethSendTransaction.getResult());
        // poll for transaction response via org.web3j.protocol.Web3j.ethGetTransactionReceipt(<txHash>)

    }

    private static byte getV(String publicKey, byte[] hashedTransaction, byte[] r, byte[] s) {
        ECDSASignature sig = new ECDSASignature(new BigInteger(r), new BigInteger(s));
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger k = recoverFromSignature(i, sig, hashedTransaction);
            if (k != null && k.equals(new BigInteger(Utils.hexStringToByteArray(publicKey)))) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException(
                    "Could not construct a recoverable key. This should never happen.");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        return (byte) headerByte;
    }

    private static BigInteger recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        verifyPrecondition(recId >= 0, "recId must be positive");
        verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        verifyPrecondition(message != null, "message cannot be null");

        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));

        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        }

        org.spongycastle.math.ec.ECPoint R = decompressKey(x, (recId & 1) == 1);

        if (!R.multiply(n).isInfinity()) {
            return null;
        }

        BigInteger e = new BigInteger(1, message);

        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        org.spongycastle.math.ec.ECPoint q = org.spongycastle.math.ec.ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);

        byte[] qBytes = q.getEncoded(false);
        // We remove the prefix
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }

    private static org.spongycastle.math.ec.ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        org.spongycastle.asn1.x9.X9IntegerConverter x9 = new org.spongycastle.asn1.x9.X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    public static BigInteger getNextNonce(Web3j web3j, String etherAddress) {
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                    etherAddress, DefaultBlockParameterName.LATEST).send();

            return ethGetTransactionCount.getTransactionCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] extractR(byte[] signature) throws Exception {
        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
        int lengthR = signature[startR + 1];
        return Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR);
    }

    private static byte[] extractS(byte[] signature) throws Exception {
        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
        int lengthR = signature[startR + 1];
        int startS = startR + 2 + lengthR;
        int lengthS = signature[startS + 1];
        return Arrays.copyOfRange(signature, startS + 2, startS + 2 + lengthS);
    }
}
