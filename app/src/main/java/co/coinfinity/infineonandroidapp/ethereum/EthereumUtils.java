package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.Utils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static org.web3j.crypto.TransactionEncoder.encode;

import static co.coinfinity.AppConstants.TAG;

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

    public static void sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String from, String to, BigInteger value, Tag tagFromIntent) {

        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                getNextNonce(web3, from), gasPrice, gasLimit, to, value);

        Transaction tx = new Transaction(
                getNextNonce(web3, from).toByteArray(),
                gasPrice.toByteArray(),
                gasLimit.toByteArray(),
                Utils.hexStringToByteArray(to),
                value.toByteArray(),
                new byte[0], // empty data field for now, we will need data for ERC-20 transfers
                new Integer(3) // Chain ID: Production=1, Ropsten=3
        );

        // This is what needs to be signed for Ethereum:
        byte[] rawTxHash = tx.getRawHash();

        //SIGN transaction
        String txSignature = null;
        try {
            byte[] encodedTransaction = encode(rawTransaction);
            final byte[] hashedTransaction = Hash.sha3(encodedTransaction);
            signedMessage = NfcUtils.signTransaction(tagFromIntent, 0x01, hashedTransaction);
        } catch (IOException e) {
            Log.e(TAG, "exception while signing", e);
        }

        // TODO: die signature muss hier och gemeinsam mit der rawTransaction gemeinsam serialisiert werden und das ergebnis wird dann gebroadcastet

        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3.ethSendRawTransaction("0x" + signedMessage).sendAsync().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String transactionHash = ethSendTransaction.getTransactionHash();
        Log.d("WEB3J", "TransactionHash: " + transactionHash);
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
}
