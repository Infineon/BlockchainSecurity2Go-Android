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
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import static co.coinfinity.AppConstants.ROPSTEN_URI;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_LIMIT;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_PRICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NfcUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class VotingUtilsTest {

    private static final String CONTRACT_ADDRESS = "0x6e670c473a2ad5894ae354b832ad4badf1d919bf";

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

        final TransactionReceipt transactionReceipt = VotingUtils.vote(
                CONTRACT_ADDRESS, isoDep, Numeric.toHexStringNoPrefixZeroPadded(TransactionSigner.credentials.getEcKeyPair().getPublicKey(), 128), TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT, null, ROPSTEN_URI);

        System.out.println(transactionReceipt.getTransactionHash());
        assertNotNull(transactionReceipt.getTransactionHash());
        assertEquals("0x1", transactionReceipt.getStatus());
    }

    @Test
    public void testWhitelistedSenderAddresses() throws Exception {
        final StaticArray4<Address> addresses = VotingUtils.whitelistedSenderAddresses(CONTRACT_ADDRESS, TransactionSigner.credentials.getAddress(), GAS_PRICE, GAS_LIMIT, ROPSTEN_URI);

        assertEquals(4, addresses.getValue().size());
    }
}