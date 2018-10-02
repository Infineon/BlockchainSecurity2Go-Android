package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.tech.IsoDep;
import android.util.Log;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static co.coinfinity.AppConstants.TAG;

/**
 * Extends web3j Transaction manager, to create ETH transactions using
 * the Infineon card for signing.
 */
public class NfcTransactionManager extends TransactionManager {

    private IsoDep tag;
    private String publicKey;
    private String fromAddress;

    /**
     * Create Nfc Transaction manager.
     *
     * @param web3j
     * @param fromAddress
     * @param tag
     * @param publicKey
     */
    public NfcTransactionManager(Web3j web3j, String fromAddress, IsoDep tag, String publicKey) {
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
            return EthereumUtils.sendTransaction(gasPrice, gasLimit, fromAddress, to, value, tag,
                    publicKey, data);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ether transaction", e);
        }
        return null;
    }
}
