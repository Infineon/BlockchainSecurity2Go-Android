package co.coinfinity.infineonandroidapp.ethereum;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import org.web3j.contracts.token.ERC20Contract;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static co.coinfinity.AppConstants.CHAIN_URL;

/**
 * Utils class used for interaction with ERC20 tokens.
 */
public class Erc20Utils {

    /**
     * Send ERC-20 compatible tokens async
     *
     * @param ercContract
     * @param tag
     * @param publicKey
     * @param from
     * @param to
     * @param amount
     * @param gasPrice
     * @param gasLimit
     * @return transaction receipt
     * @throws Exception on errors
     */
    public static Future<TransactionReceipt> sendErc20Tokens(String ercContract, IsoDep tag, String publicKey, String from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, Activity activity) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));

        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey, activity);

        ERC20Contract erc = ERC20Contract.load(ercContract, web3j, transactionManager, gasPrice, gasLimit);

        final RemoteCall<TransactionReceipt> transfer = erc.transfer(to, amount);

        return transfer.sendAsync();
    }

    /**
     * Get token balance for a contract async
     *
     * @param ercContract
     * @param ethAddress
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static BigInteger getErc20Balance(String ercContract, String ethAddress) throws ExecutionException, InterruptedException {
        if (ercContract != null && !ercContract.equals("") && ethAddress != null && !ethAddress.equals("")) {
            Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
            ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(web3j, ethAddress);

            ERC20Contract erc = ERC20Contract.load(ercContract, web3j, transactionManager, BigInteger.ZERO, BigInteger.ZERO);

            return erc.balanceOf(ethAddress).sendAsync().get();
        }

        return null;
    }
}
