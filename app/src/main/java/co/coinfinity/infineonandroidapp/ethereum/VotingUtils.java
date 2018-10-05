package co.coinfinity.infineonandroidapp.ethereum;

import android.app.Activity;
import android.nfc.Tag;
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

public class VotingUtils {

    public static void vote(String contractAddress, IsoDep tag, String publicKey, String from, String votingName, int vote, BigInteger gasPrice, BigInteger gasLimit, Activity activity) throws Exception {
        Voting contract = prepareWriteVotingContract(contractAddress, tag, publicKey, from, gasPrice, gasLimit);
        contract.castVote(new Utf8String(votingName), new Uint8(vote)).send();
    }

    public static BigInteger getVotersAnswer(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final Uint8 voted = contract.thisVotersChoice().send();
        return voted.getValue();
    }

    public static Bool voterExists(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        return contract.thisVoterExists().send();
    }

    public static String getVotersName(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final Utf8String votersName = contract.thisVotersName().send();
        return votersName.getValue();
    }

    public static List<Uint32> getCurrentResult(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        final DynamicArray<Uint32> votersName = contract.currentResult().send();
        return votersName.getValue();
    }

    private static Voting prepareWriteVotingContract(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey, activity);

        return Voting.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    private static Voting prepareReadOnlyVotingContract(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        TransactionManager transactionManager = new ReadonlyTransactionManager(web3j, from);

        return Voting.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
