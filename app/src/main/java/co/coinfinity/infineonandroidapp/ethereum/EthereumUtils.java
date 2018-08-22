package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.support.constraint.Constraints;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.Utils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
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

import static android.support.constraint.Constraints.TAG;
import static org.web3j.crypto.TransactionEncoder.encode;

public class EthereumUtils {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    private static final String HTTPS = "https://";
    private static final String BASEURL = ".infura.io/v3/7b40d72779e541a498cb0da69aa418a2";
    private static final String ROPSTEN_TESTNET = HTTPS + "ropsten" + BASEURL;
    private static final String MAINNET = HTTPS + "mainnet" + BASEURL;

    private static final byte ROPSTEN_CHAIN_ID = 3;

    public static EthBalanceBean getBalance(String ethAddress) {
        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService(ROPSTEN_TESTNET));

        BigInteger wei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.LATEST);
        BigDecimal ether = Convert.fromWei(wei.toString(), Convert.Unit.ETHER);

        BigInteger unconfirmedWei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.PENDING);
        BigDecimal unconfirmedEther = Convert.fromWei(unconfirmedWei.toString(), Convert.Unit.ETHER);

        return new EthBalanceBean(wei, ether, unconfirmedWei, unconfirmedEther);
    }

    private static BigInteger getBalanceFromApi(Web3j web3, String ethAddress, DefaultBlockParameterName defaultBlockParameterName) {
        BigInteger wei = null;
        try {
            EthGetBalance ethGetBalance = web3
                    .ethGetBalance(ethAddress, defaultBlockParameterName)
                    .send();
            if (ethGetBalance != null) {
                wei = ethGetBalance.getBalance();
            }
        } catch (IOException e) {
            Log.e(TAG, "exception while reading balance from api", e);
        }

        return wei;
    }

//    static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
//    static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());

    public static EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String from, String to, BigInteger value, Tag tagFromIntent, String publicKey) {
        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService(ROPSTEN_TESTNET));

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                getNextNonce(web3, from), gasPrice, gasLimit, to, value);

        //SIGN transaction
        String hexValue = null;
        try {
            byte[] encodedTransaction = encode(rawTransaction, ROPSTEN_CHAIN_ID);
            final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
//            final byte[] signatureData = NfcUtilsMock.signTransaction(tagFromIntent, 0x00, hashedTransaction);
            final byte[] signatureData = NfcUtils.signTransaction(tagFromIntent, 0x00, hashedTransaction);

            Log.d(Constraints.TAG, "signed transaction: " + Utils.bytesToHex(signatureData));

            byte[] r = Bytes.trimLeadingZeroes(extractR(signatureData));
            byte[] s = Bytes.trimLeadingZeroes(extractS(signatureData));
            Log.d(TAG, "r: " + Utils.bytesToHex(r));
            Log.d(TAG, "s: " + Utils.bytesToHex(s));

            s = getCanonicalisedS(r, s);
            Log.d(TAG, "s canonicalised: " + Utils.bytesToHex(s));

//            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
//            byte[] pubKey = Utils.hexStringToByteArray(publicKey);
//            byte[] prefix ={
//                    0x04
//            };
//            pubKey = Utils.combineByteArrays(prefix, pubKey);
//
//            signer.init(false, new ECPublicKeyParameters(curve.getCurve().decodePoint(pubKey), domain));
//            signer.init(false, bpubKey);
//            final boolean b = signer.verifySignature(hashedTransaction, new BigInteger(1, r), new BigInteger(1, s));
//            Log.d(TAG, "###########SIGNATURE: " + b);

            byte v = getV(publicKey, hashedTransaction, r, s);
            Log.d(TAG, "v: " + v);

            Sign.SignatureData signature = new Sign.SignatureData(v, r, s);
            signature = TransactionEncoder.createEip155SignatureData(signature, ROPSTEN_CHAIN_ID);

            //TODO calls private method of web3j lib
            Class c = TransactionEncoder.class;
            Object obj = c.newInstance();
            Method m = c.getDeclaredMethod("encode", RawTransaction.class, Sign.SignatureData.class);
            m.setAccessible(true);
            final byte[] invoke = (byte[]) m.invoke(obj, rawTransaction, signature);
            hexValue = Numeric.toHexString(invoke);
            Log.d(TAG, "hexValue: " + hexValue);

        } catch (Exception e) {
            Log.e(TAG, "exception while signing", e);
        }

        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
        } catch (IOException e) {
            Log.e(TAG, "exception while sending transaction", e);
        }
        assert ethSendTransaction != null;
        String transactionHash = ethSendTransaction.getTransactionHash();
        Log.d(TAG, "TransactionHash: " + transactionHash);
        Log.d(TAG, "TransactionResult: " + ethSendTransaction.getResult());
        if (ethSendTransaction.getError() != null) {
            Log.d(TAG, "TransactionError: " + ethSendTransaction.getError().getMessage());
        }

        return ethSendTransaction;
    }

    public static BigInteger getNextNonce(Web3j web3j, String etherAddress) {
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                    etherAddress, DefaultBlockParameterName.LATEST).send();

            return ethGetTransactionCount.getTransactionCount();
        } catch (IOException e) {
            Log.e(TAG, "exception while getting next nonce", e);
        }
        return null;
    }

    private static byte[] getCanonicalisedS(byte[] r, byte[] s) {
        ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        ecdsaSignature = ecdsaSignature.toCanonicalised();
        return ecdsaSignature.s.toByteArray();
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

    private static byte getV(String publicKey, byte[] hashedTransaction, byte[] r, byte[] s) {
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {

            BigInteger k = null;
            //TODO calls private method of web3j lib
            Class c = Sign.class;
            Object obj = null;
            try {
                obj = c.newInstance();
                Method m = c.getDeclaredMethod("recoverFromSignature", int.class, ECDSASignature.class, byte[].class);
                m.setAccessible(true);

                k = (BigInteger) m.invoke(obj, i, sig, hashedTransaction);
            } catch (Exception e) {
                Log.e(TAG, "exception while calling private method recoverFromSignature", e);
            }

            if (k != null && k.equals(new BigInteger(1, Utils.hexStringToByteArray(publicKey)))) {
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
}
