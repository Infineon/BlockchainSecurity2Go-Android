package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.ethereum.NfcTransactionManager;
import co.coinfinity.infineonandroidapp.ethereum.contract.Voting;
import co.coinfinity.infineonandroidapp.utils.InvalidContractException;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;

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
    public static TransactionReceipt vote(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity, String url) throws Exception {
        Voting contract = prepareWriteVotingContract(contractAddress, tag, publicKey, from, gasPrice, gasLimit, activity, url);
        return contract.castVote().send();
    }


    /**
     * Assert that the given contract really is an instance of our contract
     *
     * @param contract
     * @throws InvalidContractException
     * @throws IOException
     */
    private static void assertContract(Voting contract) throws InvalidContractException, IOException {
        if (!contract.isValid()) {
            throw new InvalidContractException("The bytecode at this address does not match our voting contract! Are yu using the correct address?");
        }
    }

    public static StaticArray4<Address> whitelistedSenderAddresses(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit, String url) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit, url);
        assertContract(contract);
        // check if the contract deployed at this address is an instance of our Voting contract
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
    private static Voting prepareWriteVotingContract(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity, String url) {
        Web3j web3j = Web3jFactory.build(new HttpService(url));
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
    private static Voting prepareReadOnlyVotingContract(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit, String url) {
        Web3j web3j = Web3jFactory.build(new HttpService(url));
        TransactionManager transactionManager = new ReadonlyTransactionManager(web3j, from);

        return Voting.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
