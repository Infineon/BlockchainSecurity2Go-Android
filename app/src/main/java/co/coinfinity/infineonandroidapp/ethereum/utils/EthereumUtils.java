package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.nfc.tech.IsoDep;
import android.util.Log;
import android.util.Pair;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GenerateSignatureResponseApdu;
import co.coinfinity.infineonandroidapp.utils.ByteUtils;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;
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

import static co.coinfinity.AppConstants.TAG;
import static org.web3j.crypto.TransactionEncoder.encode;

/**
 * Utils class used to interact with Ethereum Blockchain.
 */
public class EthereumUtils {

    /**
     * this method reads the balance by ether address and returns a balance object.
     *
     * @param ethAddress
     * @return EthBalanceBean containing information about the balance
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static EthBalanceBean getBalance(String ethAddress, String url) throws Exception {
        Web3j web3 = Web3jFactory.build(new HttpService(url));

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

    /**
     * this method reads the ether balance from api via web3j
     *
     * @param web3                      used web3j
     * @param ethAddress                ether address
     * @param defaultBlockParameterName
     * @return the ether balance itself
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static BigInteger getBalanceFromApi(Web3j web3, String ethAddress,
                                                DefaultBlockParameterName defaultBlockParameterName) throws Exception {
        BigInteger wei = null;
        EthGetBalance ethGetBalance = web3
                .ethGetBalance(ethAddress, defaultBlockParameterName)
                .sendAsync().get();
        if (ethGetBalance != null) {
            wei = ethGetBalance.getBalance();
        }

        return wei;
    }

    /**
     * Send an Ethereum transaction.
     *
     * @param gasPrice
     * @param gasLimit
     * @param from
     * @param to
     * @param value
     * @param isoTag
     * @param publicKey
     * @param data
     * @return Status object of the sent transaction
     * @throws Exception
     */
    public static Pair<EthSendTransaction, GenerateSignatureResponseApdu> sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String from,
                                                                                          String to, BigInteger value, IsoDep isoTag,
                                                                                          String publicKey, String data, String url, byte chainId, int keyIndex, byte[] pin) throws Exception {
        Web3j web3 = Web3jFactory.build(new HttpService(url));

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                getNextNonce(web3, from), gasPrice, gasLimit, to, value, data);

        byte[] encodedTransaction = encode(rawTransaction, chainId);

        final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
        final GenerateSignatureResponseApdu signedTransaction = NfcUtils.generateSignature(IsoTagWrapper.of(isoTag), keyIndex, hashedTransaction, pin);

        Log.d(TAG, String.format("signed transaction: %s", ByteUtils.bytesToHex(signedTransaction.getSignature())));

        byte[] r = Bytes.trimLeadingZeroes(extractR(signedTransaction.getSignature()));
        byte[] s = Bytes.trimLeadingZeroes(extractS(signedTransaction.getSignature()));
        Log.d(TAG, String.format("r: %s", ByteUtils.bytesToHex(r)));
        Log.d(TAG, String.format("s: %s", ByteUtils.bytesToHex(s)));

        s = getCanonicalisedS(r, s);
        Log.d(TAG, String.format("s canonicalised: %s", ByteUtils.bytesToHex(s)));

        byte v = getV(publicKey, hashedTransaction, r, s);
        Log.d(TAG, String.format("v: %s", v));

        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
        signatureData = TransactionEncoder.createEip155SignatureData(signatureData, chainId);

        String hexValue = Numeric.toHexString(TransactionEncoder.encode(rawTransaction, signatureData));
        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction != null && ethSendTransaction.getError() != null) {
            Log.e(TAG, String.format("TransactionError: %s", ethSendTransaction.getError().getMessage()));
            //TODO is this needed because it doesnt work with sig counter message
//            throw new RuntimeException(String.format("TransactionError: %s",
//                    ethSendTransaction.getError().getMessage()));
        }
        return new Pair<>(ethSendTransaction, signedTransaction);
    }

    /**
     * this method checks the next nonce to use and returns it
     *
     * @param web3j        web3j to use
     * @param etherAddress ether address
     * @return the next nonce
     * @throws IOException
     */
    public static BigInteger getNextNonce(Web3j web3j, String etherAddress) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = null;
        ethGetTransactionCount = web3j.ethGetTransactionCount(
                etherAddress, DefaultBlockParameterName.PENDING).send();

        Log.d(TAG, String.format("Nonce: %s", ethGetTransactionCount.getTransactionCount()));
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

            if (k != null && k.equals(new BigInteger(1, ByteUtils.fromHexString(publicKey)))) {
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
