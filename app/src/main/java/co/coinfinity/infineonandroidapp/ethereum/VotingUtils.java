package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.Tag;
import android.util.Log;
import co.coinfinity.infineonandroidapp.ethereum.contract.Voting;
import co.coinfinity.infineonandroidapp.nfc.NfcTransactionManager;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.List;

import static co.coinfinity.AppConstants.CHAIN_URL;
import static co.coinfinity.AppConstants.TAG;

public class VotingUtils {

    public static void vote(String contractAddress, Tag tag, String publicKey, String from, String votingName, int vote, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        Voting contract = prepareWriteVotingContract(contractAddress, tag, publicKey, from, gasPrice, gasLimit);
        contract.giveVote(new Utf8String(votingName), new Uint8(vote)).send();
    }

    public static int getVotersAnswer(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        try {
            final Uint8 voted = contract.getVotersAnswer().send();
            return voted.getValue().intValue();
        } catch (Exception e) {
            Log.e(TAG, "exception while voting: ", e);
        }
        return -1;
    }

    public static String getVotersName(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        try {
            final Utf8String votersName = contract.getVotersName().send();
            return votersName.getValue();
        } catch (Exception e) {
            Log.e(TAG, "exception while voting: ", e);
        }
        return null;
    }

    public static List<Uint8> getAnswerCounts(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Voting contract = prepareReadOnlyVotingContract(contractAddress, from, gasPrice, gasLimit);
        try {
            final DynamicArray<Uint8> votersName = contract.getAnswerCounts().send();
            return votersName.getValue();
        } catch (Exception e) {
            Log.e(TAG, "exception while getting answer count: ", e);
        }
        return null;
    }

    private static Voting prepareWriteVotingContract(String contractAddress, Tag tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit) {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey);

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
