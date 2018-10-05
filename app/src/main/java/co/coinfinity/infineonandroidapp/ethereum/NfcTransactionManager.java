package co.coinfinity.infineonandroidapp.ethereum;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.R;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
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
    private Activity activity;

    /**
     * Create Nfc Transaction manager.
     *
     * @param web3j
     * @param fromAddress
     * @param tag
     * @param publicKey
     */
    public NfcTransactionManager(Web3j web3j, String fromAddress, IsoDep tag, String publicKey, Activity activity) {
        super(web3j, fromAddress);
        this.fromAddress = fromAddress;
        this.tag = tag;
        this.publicKey = publicKey;
        this.activity = activity;
    }

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) {
        try {
            final EthSendTransaction ethSendTransaction = EthereumUtils.sendTransaction(gasPrice, gasLimit, fromAddress, to, value, tag, publicKey, new NfcUtils(), data);
            activity.runOnUiThread(() -> Toast.makeText(activity, R.string.send_success, Toast.LENGTH_SHORT).show());
            return ethSendTransaction;
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ether transaction", e);
            activity.runOnUiThread(() -> Toast.makeText(activity, "Could not send transaction!", Toast.LENGTH_SHORT).show());
        }
        return null;
    }
}
