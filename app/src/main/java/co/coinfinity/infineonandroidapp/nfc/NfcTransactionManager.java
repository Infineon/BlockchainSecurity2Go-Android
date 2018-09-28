package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static co.coinfinity.AppConstants.TAG;

public class NfcTransactionManager extends TransactionManager {

    private Tag tag;
    private String publicKey;
    private String fromAddress;

    public NfcTransactionManager(Web3j web3j, String fromAddress, Tag tag, String publicKey) {
        super(web3j, fromAddress);
        this.fromAddress = fromAddress;
        this.tag = tag;
        this.publicKey = publicKey;
    }

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) {
        try {
            return EthereumUtils.sendTransaction(gasPrice, gasLimit, fromAddress, to, value, tag, publicKey, new NfcUtils(), data);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ether transaction", e);
        }
        return null;
    }
}
