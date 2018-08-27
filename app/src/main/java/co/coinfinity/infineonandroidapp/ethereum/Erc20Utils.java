package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.support.constraint.Constraints;
import android.util.Log;
import org.web3j.contracts.token.ERC20Contract;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static co.coinfinity.AppConstants.CHAIN_URL;

public class Erc20Utils {

    public static TransactionReceipt sendErc20Tokens(String ercContract, Tag tag, String publicKey, String from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));

        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey);

        ERC20Contract erc = ERC20Contract.load(ercContract, web3j, transactionManager, gasPrice, gasLimit);

        final RemoteCall<TransactionReceipt> transfer = erc.transfer(to, amount);
        final TransactionReceipt transactionReceipt = transfer.send();
        Log.d(Constraints.TAG, "ERC20 Transaction Hash: " + transactionReceipt.getTransactionHash());

        return transactionReceipt;
    }

    public static BigInteger getErc20Balance(String ercContract, String ethAddress) throws Exception {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));

        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(web3j, ethAddress);

        ERC20Contract erc = ERC20Contract.load(ercContract, web3j, transactionManager, BigInteger.ZERO, BigInteger.ZERO);

        return erc.balanceOf(ethAddress).sendAsync().get();
    }


}
