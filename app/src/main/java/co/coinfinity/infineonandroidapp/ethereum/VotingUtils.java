package co.coinfinity.infineonandroidapp.ethereum;

import android.app.Activity;
import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.ethereum.contract.Voting;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.List;

import static co.coinfinity.AppConstants.CHAIN_URL;

/**
 * this class is interacting with the Ethereum voting smart contract
 */
public class VotingUtils {

    /**
     * give a vote for defined ether address on contract
     *
     * @param contractAddress
     * @param tag
     * @param publicKey
     * @param from
     * @param votingName
     * @param vote
     * @param gasPrice
     * @param gasLimit
     * @param activity
     * @throws Exception
     */
    public static void vote(String contractAddress, IsoDep tag, String publicKey, String from, String votingName, int vote, BigInteger gasPrice, BigInteger gasLimit, Activity activity) throws Exception {
        Voting contract = prepareWriteVotingContract(contractAddress, tag, publicKey, from, gasPrice, gasLimit, activity);
        contract.castVote(new Utf8String(votingName), new Uint8(vote)).send();
    }

    /**
     * get answer of voter if already voted
     * @param contractAddress
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @return
     * @throws Exception
     */
    public static BigInteger getVotersAnswer(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final Uint8 voted = contract.thisVotersChoice().send();
        return voted.getValue();
    }

    /**
     * checks if voter already voted or not
     * @param contractAddress
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @return
     * @throws Exception
     */
    public static Bool voterExists(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        return contract.thisVoterExists().send();
    }

    /**
     * get the name of the voter
     * @param contractAddress
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @return
     * @throws Exception
     */
    public static String getVotersName(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final Utf8String votersName = contract.thisVotersName().send();
        return votersName.getValue();
    }

    /**
     * get current amount of votes per answer
     * @param contractAddress
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @return
     * @throws Exception
     */
    public static List<Uint32> getCurrentResult(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final DynamicArray<Uint32> votersName = contract.currentResult().send();
        return votersName.getValue();
    }

    /**
     * loads a new voting contract
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
