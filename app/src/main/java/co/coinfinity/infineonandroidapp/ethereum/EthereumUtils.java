package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.ByteUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static co.coinfinity.AppConstants.*;
import static org.web3j.crypto.TransactionEncoder.encode;

public class EthereumUtils {

    public static EthBalanceBean getBalance(String ethAddress) throws ExecutionException, InterruptedException {
        Web3j web3 = Web3jFactory.build(new HttpService(CHAIN_URL));

        BigInteger wei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.LATEST);
        if (wei == null) {
            wei = new BigInteger("0");
        }
        BigDecimal ether = Convert.fromWei(wei.toString(), Convert.Unit.ETHER);

        BigInteger unconfirmedWei = getBalanceFromApi(web3, ethAddress, DefaultBlockParameterName.PENDING);
        if (unconfirmedWei == null) {
            unconfirmedWei = new BigInteger("0");
        }
        unconfirmedWei = unconfirmedWei.subtract(wei);
        BigDecimal unconfirmedEther = Convert.fromWei(unconfirmedWei.toString(), Convert.Unit.ETHER);

        return new EthBalanceBean(wei, ether, unconfirmedWei, unconfirmedEther);
    }

    private static BigInteger getBalanceFromApi(Web3j web3, String ethAddress, DefaultBlockParameterName defaultBlockParameterName) throws ExecutionException, InterruptedException {
        BigInteger wei = null;
        EthGetBalance ethGetBalance = web3
                .ethGetBalance(ethAddress, defaultBlockParameterName)
                .sendAsync().get();
        if (ethGetBalance != null) {
            wei = ethGetBalance.getBalance();
        }

        return wei;
    }

    public static EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String from, String to, BigInteger value, Tag tagFromIntent, String publicKey, NfcUtils nfcUtils, String data) throws Exception {
        Web3j web3 = Web3jFactory.build(new HttpService(CHAIN_URL));

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                getNextNonce(web3, from), gasPrice, gasLimit, to, value, data);

        String hexValue = null;
        byte[] encodedTransaction = encode(rawTransaction, CHAIN_ID);
        final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
        final byte[] signedTransaction = nfcUtils.signTransaction(tagFromIntent, CARD_ID, hashedTransaction);

        Log.d(TAG, "signed transaction: " + ByteUtils.bytesToHex(signedTransaction));

        byte[] r = Bytes.trimLeadingZeroes(extractR(signedTransaction));
        byte[] s = Bytes.trimLeadingZeroes(extractS(signedTransaction));
        Log.d(TAG, "r: " + ByteUtils.bytesToHex(r));
        Log.d(TAG, "s: " + ByteUtils.bytesToHex(s));

        s = getCanonicalisedS(r, s);
        Log.d(TAG, "s canonicalised: " + ByteUtils.bytesToHex(s));

        byte v = getV(publicKey, hashedTransaction, r, s);
        Log.d(TAG, "v: " + v);

        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
        signatureData = TransactionEncoder.createEip155SignatureData(signatureData, CHAIN_ID);

        //calls private method form web3j lib
        hexValue = Numeric.toHexString(TransactionEncoder.encode(rawTransaction, signatureData));
        Log.d(TAG, "hexValue: " + hexValue);

        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction != null) {
            String transactionHash = ethSendTransaction.getTransactionHash();

            Log.d(TAG, "TransactionHash: " + transactionHash);
            Log.d(TAG, "TransactionResult: " + ethSendTransaction.getResult());
            if (ethSendTransaction.getError() != null) {
                Log.d(TAG, "TransactionError: " + ethSendTransaction.getError().getMessage());
            }

        }
        return ethSendTransaction;
    }

    public static BigInteger getNextNonce(Web3j web3j, String etherAddress) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = null;
        ethGetTransactionCount = web3j.ethGetTransactionCount(
                etherAddress, DefaultBlockParameterName.PENDING).send();

        Log.d(TAG, "Nonce: " + ethGetTransactionCount.getTransactionCount());
        return ethGetTransactionCount.getTransactionCount();
    }

    private static byte[] getCanonicalisedS(byte[] r, byte[] s) {
        ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        ecdsaSignature = ecdsaSignature.toCanonicalised();
        return ecdsaSignature.s.toByteArray();
    }

    private static byte[] extractR(byte[] signature) {
        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
        int lengthR = signature[startR + 1];
        return Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR);
    }

    private static byte[] extractS(byte[] signature) {
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

            //calls private method form web3j lib
            BigInteger k = Sign.recoverFromSignature(i, sig, hashedTransaction);

            if (k != null && k.equals(new BigInteger(1, ByteUtils.hexStringToByteArray(publicKey)))) {
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
