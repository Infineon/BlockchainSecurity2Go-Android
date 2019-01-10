package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static co.coinfinity.AppConstants.ROPSTEN_URI;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_LIMIT;
import static co.coinfinity.infineonandroidapp.ethereum.utils.TransactionSigner.GAS_PRICE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NfcUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class EthereumUtilsTest {

    @Mock
    IsoDep isoDep;

    @Test
    public void testGetBalanceTest() throws Exception {
        final EthBalanceBean balance = EthereumUtils.getBalance(TransactionSigner.credentials.getAddress(), ROPSTEN_URI);

        assertTrue(balance.getEther().doubleValue() > 0);
        assertTrue(balance.getWei().doubleValue() > 0);
        assertEquals(new BigDecimal("0"), balance.getUnconfirmedEther());
        assertEquals(new BigInteger("0"), balance.getUnconfirmedWei());
    }

    @Test
    public void testSendTransaction() throws Exception {
        PowerMockito.mockStatic(NfcUtils.class);
        when(NfcUtils.generateSignature(any(), anyInt(), any(), any())).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    return TransactionSigner.signTransaction((byte[]) args[2]);
                });

        final EthSendTransaction ethSendTransaction = EthereumUtils.sendTransaction(
                GAS_PRICE,
                GAS_LIMIT,
                TransactionSigner.credentials.getAddress(),
                "0xa8e5590D3E1377BAfac30d3D3AB5779A62e0FF28",
                Convert.toWei("0.002", Convert.Unit.ETHER).toBigInteger(),
                isoDep,
                Numeric.toHexStringNoPrefixZeroPadded(TransactionSigner.credentials.getEcKeyPair().getPublicKey(), 128),
                "", ROPSTEN_URI, ChainId.ROPSTEN, 1, null);

        assertNull(ethSendTransaction.getError());
        System.out.println(ethSendTransaction.getTransactionHash());
        assertThat(ethSendTransaction.getResult(), containsString("0x"));
    }

    @Test
    public void testGetNextNonce() throws IOException {
        Web3j web3 = Web3jFactory.build(new HttpService(ROPSTEN_URI));

        final BigInteger nextNonce = EthereumUtils.getNextNonce(web3, "0xfd37944e59fB227043F1F53Ca6Aef1C953684f46");

        assertTrue(nextNonce.intValue() > 0);
    }
}