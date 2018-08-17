package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.Utils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
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
import static org.web3j.crypto.TransactionEncoder.encode;

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
//            byte[] encodedTransaction = encode(rawTransaction,Integer.valueOf(3).byteValue());
            byte[] encodedTransaction = encode(rawTransaction);
            final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
            signedTransaction = NfcUtils.signTransaction(tagFromIntent, 0x01, hashedTransaction);
            Log.d(TAG, "signedTransaction: " + signedTransaction);
            assert signedTransaction != null;
            final byte[] signatureData = Utils.hexStringToByteArray(signedTransaction);

            final byte[] r = Bytes.trimLeadingZeroes(extractR(signatureData));
            final byte[] s = Bytes.trimLeadingZeroes(extractS(signatureData));
            final byte v = getRecoveryId(r, s, hashedTransaction, Utils.hexStringToByteArray(publicKey));
            Log.d(TAG, "r: " + Utils.bytesToHex(r));
            Log.d(TAG, "s: " + Utils.bytesToHex(s));
            Log.d(TAG, "v: " + v);

//            byte vNeu = (byte) (v + (Integer.valueOf(3).byteValue() << 1) + 8);
//            Sign.SignatureData signature = new Sign.SignatureData(vNeu,r,s);

//            Sign.SignatureData signature = new Sign.SignatureData(vNeu, new byte[] {}, new byte[] {});
            Sign.SignatureData signature = new Sign.SignatureData(Integer.valueOf(3 * 2 + 35 + Byte.valueOf(v).intValue()).byteValue(), r, s);

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
        // poll for transaction response via org.web3j.protocol.Web3j.ethGetTransactionReceipt(<txHash>)

    }

    private static BigInteger getNextNonce(Web3j web3j, String etherAddress) {
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

    /**
     * Determine the recovery ID for the given signature and public key.
     *
     * <p>Any signed message can resolve to one of two public keys due to the nature ECDSA. The
     * recovery ID provides information about which one it is, allowing confirmation that the message
     * was signed by a specific key.</p>
     */
    private static byte getRecoveryId(byte[] sigR, byte[] sigS, byte[] message, byte[] publicKey) {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(SECP256K1);
        BigInteger pointN = spec.getN();
        for (int recoveryId = 0; recoveryId < 2; recoveryId++) {
            try {
                BigInteger pointX = new BigInteger(1, sigR);

                X9IntegerConverter x9 = new X9IntegerConverter();
                byte[] compEnc = x9.integerToBytes(pointX, 1 + x9.getByteLength(spec.getCurve()));
                compEnc[0] = (byte) ((recoveryId & 1) == 1 ? 0x03 : 0x02);
                ECPoint pointR = spec.getCurve().decodePoint(compEnc);
                if (!pointR.multiply(pointN).isInfinity()) {
                    continue;
                }

                BigInteger pointE = new BigInteger(1, message);
                BigInteger pointEInv = BigInteger.ZERO.subtract(pointE).mod(pointN);
                BigInteger pointRInv = new BigInteger(1, sigR).modInverse(pointN);
                BigInteger srInv = pointRInv.multiply(new BigInteger(1, sigS)).mod(pointN);
                BigInteger pointEInvRInv = pointRInv.multiply(pointEInv).mod(pointN);
                ECPoint pointQ = ECAlgorithms.sumOfTwoMultiplies(spec.getG(), pointEInvRInv, pointR, srInv);
                byte[] pointQBytes = pointQ.getEncoded(false);
                boolean matchedKeys = true;
                for (int j = 0; j < publicKey.length; j++) {
                    if (pointQBytes[j] != publicKey[j]) {
                        matchedKeys = false;
                        break;
                    }
                }
                if (!matchedKeys) {
                    continue;
                }
                return (byte) (0xFF & recoveryId);
            } catch (Exception e) {
                Log.e(TAG, "exception on getRecoveryId", e);
            }
        }

        return (byte) 0xFF;
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
