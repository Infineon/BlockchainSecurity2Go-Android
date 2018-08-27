package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

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
        return EthereumUtils.sendTransaction(gasPrice, gasLimit, fromAddress, to, value, tag, publicKey, new NfcUtils(), data);
    }
}
