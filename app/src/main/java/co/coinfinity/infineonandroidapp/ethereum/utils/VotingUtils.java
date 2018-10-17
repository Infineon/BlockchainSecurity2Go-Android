package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.ethereum.NfcTransactionManager;
import co.coinfinity.infineonandroidapp.ethereum.contract.Voting;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static co.coinfinity.AppConstants.CHAIN_URL;

/**
 * this class is interacting with the Ethereum voting smart contract.
 */
public class VotingUtils {

    /**
     * give a vote for defined ether address on contract
     *
     * @param contractAddress
     * @param tag
     * @param publicKey
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @param activity
     * @throws Exception
     */
    public static TransactionReceipt vote(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) throws Exception {
        Voting contract = prepareWriteVotingContract(contractAddress, tag, publicKey, from, gasPrice, gasLimit, activity);
        return contract.castVote().send();
    }


    public static StaticArray4<Address> whitelistedSenderAddresses(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        return contract.whitelistedSenderAddresses().send();
    }

    /**
     * loads a new voting contract
     *
     * @param contractAddress
     * @param tag
     * @param publicKey
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @param activity
     * @return
     */
    private static Voting prepareWriteVotingContract(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey, activity);

        return Voting.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    /**
     * loads a new read only voting contract
     *
     * @param contractAddress
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @return
     */
    private static Voting prepareReadOnlyVotingContract(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        TransactionManager transactionManager = new ReadonlyTransactionManager(web3j, from);

        return Voting.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
