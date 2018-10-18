package co.coinfinity.infineonandroidapp.ethereum;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import android.util.Log;
import co.coinfinity.infineonandroidapp.R;
import co.coinfinity.infineonandroidapp.VotingActivity;
import co.coinfinity.infineonandroidapp.ethereum.utils.EthereumUtils;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

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

    /**
     * this method will be called on transaction sending
     *
     * @param gasPrice
     * @param gasLimit
     * @param to
     * @param data
     * @param value
     * @return ether object with transaction hash
     */
    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) {
        try {
            Log.d(TAG, "sending ETH transaction..");
            final EthSendTransaction response = EthereumUtils.sendTransaction(gasPrice, gasLimit, fromAddress, to, value, tag, publicKey, data);
            Log.d(TAG, String.format("sending ETH transaction finished with Hash: %s", response.getTransactionHash()));
            if (activity != null) {
                if ("Voting".equals(activity.getTitle().toString())) {
                    showToast(activity.getString(R.string.voted_successful), activity);
                } else {
                    showToast(activity.getString(R.string.send_success), activity);
                }
            }
            return response;
        } catch (NfcCardException e) {
            if (activity != null)
                showToast(activity.getString(R.string.operation_not_supported), activity);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ether transaction", e);
            if (!(activity instanceof VotingActivity))
                showToast(String.format("Could not send transaction: %s", e.getMessage()), activity);
        }
        return null;
    }
}
