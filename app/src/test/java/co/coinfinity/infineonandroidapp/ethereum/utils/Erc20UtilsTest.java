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
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_LIMIT;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_PRICE;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NfcUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class Erc20UtilsTest {

    private static final String ERC_CONTRACT = "0xd5ffaa5d81cfe4d4141a11d83d6d7aada39d230e";
    @Mock
    private IsoDep isoDep;

    @Test
    public void testSendErc20Tokens() throws Exception {
        PowerMockito.mockStatic(NfcUtils.class);
        when(NfcUtils.generateSignature(any(), anyInt(), any())).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    return TransactionSigner.signTransaction((byte[]) args[2]);
                });

        final TransactionReceipt transactionReceipt = Erc20Utils.sendErc20Tokens(ERC_CONTRACT, isoDep, Numeric.toHexStringNoPrefixZeroPadded(TransactionSigner.credentials.getEcKeyPair().getPublicKey(), 128), TransactionSigner.credentials.getAddress(), "0x8720af26d1810ae8fca995002ad56175e4c97b23", new BigInteger("1"), GAS_PRICE, GAS_LIMIT, null);

        System.out.println(transactionReceipt.getTransactionHash());
        assertNotNull(transactionReceipt.getTransactionHash());
        assertEquals("0x1", transactionReceipt.getStatus());
    }

    @Test
    public void testGetErc20Balance() throws Exception {
        final BigInteger erc20Balance = Erc20Utils.getErc20Balance(ERC_CONTRACT, TransactionSigner.credentials.getAddress());

        System.out.printf("Token Balance: %s%n", erc20Balance);
        assertThat(erc20Balance.intValue(),
                greaterThan(0));
    }
}