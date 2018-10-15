package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_LIMIT;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_PRICE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NfcUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class VotingUtilsTest {

    private static final String CONTRACT_ADDRESS = "0xBA0b36fcA23C1D294baA6d56672190B5699BE5D1";

    @Mock
    IsoDep isoDep;

    @Test
    public void testVote() throws Exception {
        PowerMockito.mockStatic(NfcUtils.class);
        when(NfcUtils.generateSignature(any(), anyInt(), any())).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    return TransactionSigner.signTransaction((byte[]) args[2]);
                });

        final TransactionReceipt transactionReceipt = VotingUtils.vote(CONTRACT_ADDRESS, isoDep, Numeric.toHexStringNoPrefixZeroPadded(TransactionSigner.credentials.getEcKeyPair().getPublicKey(), 128), TransactionSigner.credentials.getAddress(), "peter", 1, GAS_PRICE, GAS_LIMIT, null);

        System.out.println(transactionReceipt.getTransactionHash());
        assertNotNull(transactionReceipt.getTransactionHash());
        assertEquals("0x1", transactionReceipt.getStatus());
    }

    @Test
    public void testGetVotersAnswer() throws Exception {
        final BigInteger votersAnswer = VotingUtils.getVotersAnswer(CONTRACT_ADDRESS, TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT);

        assertEquals(1, votersAnswer.intValue());
    }

    @Test
    public void testVoterExists() throws Exception {
        final Bool exists = VotingUtils.voterExists(CONTRACT_ADDRESS, TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT);

        assertTrue(exists.getValue());
    }

    @Test
    public void testGetVotersName() throws Exception {
        final String votersName = VotingUtils.getVotersName(CONTRACT_ADDRESS, TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT);

        assertEquals("peter", votersName);
    }

    @Test
    public void testGetCurrentResult() throws Exception {
        final List<Uint32> currentResult = VotingUtils.getCurrentResult(CONTRACT_ADDRESS, TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT);

        currentResult.forEach(uint32 -> {
            System.out.println(uint32.getValue());
        });
        assertEquals(4, (long) currentResult.size());
    }
}