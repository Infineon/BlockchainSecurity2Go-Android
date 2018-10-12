package co.coinfinity.infineonandroidapp.ethereum.utils;

import org.junit.Test;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint32;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VotingUtilsTest {

    private static final String CONTRACT_ADDRESS = "0xBA0b36fcA23C1D294baA6d56672190B5699BE5D1";
    private static final String FROM_ADDRESS = "0x8720af26d1810ae8fca995002ad56175e4c97b23";
    private static final BigInteger GAS_PRICE = new BigInteger("50");
    private static final BigInteger GAS_LIMIT = new BigInteger("10000");

    @Test
    public void testVote() {
        //TODO how to sign without card?
    }

    @Test
    public void testGetVotersAnswer() throws Exception {
        final BigInteger votersAnswer = VotingUtils.getVotersAnswer(CONTRACT_ADDRESS, FROM_ADDRESS, GAS_PRICE, GAS_LIMIT);

        assertEquals(1, votersAnswer.intValue());
    }

    @Test
    public void testVoterExists() throws Exception {
        final Bool exists = VotingUtils.voterExists(CONTRACT_ADDRESS, FROM_ADDRESS, GAS_PRICE, GAS_LIMIT);

        assertTrue(exists.getValue());
    }

    @Test
    public void testGetVotersName() throws Exception {
        final String votersName = VotingUtils.getVotersName(CONTRACT_ADDRESS, FROM_ADDRESS, GAS_PRICE, GAS_LIMIT);

        assertEquals("hans", votersName);
    }

    @Test
    public void testGetCurrentResult() throws Exception {
        final List<Uint32> currentResult = VotingUtils.getCurrentResult(CONTRACT_ADDRESS, FROM_ADDRESS, GAS_PRICE, GAS_LIMIT);

        currentResult.forEach(uint32 -> {
            System.out.println(uint32.getValue());
        });
        assertEquals(4, (long) currentResult.size());
    }
}