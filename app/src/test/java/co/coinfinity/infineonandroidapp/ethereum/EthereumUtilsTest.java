package co.coinfinity.infineonandroidapp.ethereum;

import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.ethereum.contract.Voting;
import co.coinfinity.infineonandroidapp.utils.ByteWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static co.coinfinity.AppConstants.CHAIN_URL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

@RunWith(MockitoJUnitRunner.class)
public class EthereumUtilsTest {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
    //    static Credentials credentials = Credentials.create(new BigInteger("79166386603517236976726811532830064984355265773618493467297037703400211058279").toString(16),
    //            new BigInteger("1151270011825183223805235897419104860957743818568039421199126264923822213203842512949016734652068699212645732466302596308750268989266518249615792739627611").toString(16));
    static Credentials credentials = Credentials.create("0x8105dc1bcfac5c5be13da822c3cd7568ef55124ab45a2afac691f61d193cfd69");
    @Mock
    IsoDep tag;

    @Test
    public void testContract() throws Exception {
        Web3j web3j = Web3jFactory.build(new HttpService(CHAIN_URL));
        Voting contract = Voting.load(
                "0xe96398ece7be0b03b53f1ca01a23698db338cc5d", web3j, credentials, GAS_PRICE, GAS_LIMIT);
        System.out.println("TEST");
        final TransactionReceipt voted = contract.castVote(new Utf8String("daa"), new Uint8(1)).send();
        System.out.println(voted.getTransactionHash());
        final Uint8 send = contract.thisVotersChoice().send();
        System.out.println(send.getValue());
    }

    @Test
    public void getBalanceTest() throws ExecutionException, InterruptedException {
        final EthBalanceBean balance = EthereumUtils.getBalance("0xfd37944e59fB227043F1F53Ca6Aef1C953684f46");

        assertTrue(balance.getEther().doubleValue() > 0);
        assertTrue(balance.getWei().doubleValue() > 0);
        assertEquals(new BigDecimal("0"), balance.getUnconfirmedEther());
        assertEquals(new BigInteger("0"), balance.getUnconfirmedWei());
    }

    @Test
    public void sendTransaction() throws Exception {
//        when(infineonNfcUtils.signTransaction(any(Tag.class), anyInt(), any(byte[].class))).thenReturn(
//                signTransaction(ByteUtils.fromHexString("6E190B28384A62507BA107C70AF66362054D0B14B24ADD2A43606B7530C4763B")));
//        Mock.
//        when(nfcUtils.)

        //when(tag.transceive())

        final EthSendTransaction ethSendTransaction = EthereumUtils.sendTransaction(
                new BigInteger("50000000000"),
                new BigInteger("21000"),
                "0xfd37944e59fB227043F1F53Ca6Aef1C953684f46",
                "0xa8e5590D3E1377BAfac30d3D3AB5779A62e0FF28",
                new BigInteger("30000000000000000"),
                tag,
                "15fb4a64962d7e7dc369588378db28895d407ffb6baf88891b8d9815170ee3b4c1aebb09da0a8497e1aeab4c8f644adf29d67b9c92f0cfeef9ff71b5142ab25b",
                "");

        System.out.println(ethSendTransaction.getError().getMessage());
        assertNull(ethSendTransaction.getError());
        assertThat(ethSendTransaction.getResult(), containsString("0x"));
    }

    @Test
    public void getNextNonce() throws IOException {
        Web3j web3 = Web3jFactory.build(new HttpService(CHAIN_URL));

        final BigInteger nextNonce = EthereumUtils.getNextNonce(web3, "0xfd37944e59fB227043F1F53Ca6Aef1C953684f46");

        assertTrue(nextNonce.intValue() > 0);
    }


    private byte[] signTransaction(byte[] data) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(credentials.getEcKeyPair().getPrivateKey(), CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(data);

        ECDSASignature sig = new ECDSASignature(components[0], components[1]).toCanonicalised();

        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        // Write DER encoding of signature
        ByteWriter writer = new ByteWriter(1024);
        // Write tag
        writer.put((byte) 0x30);
        // Write total length

        int totalLength = 2 + r.length + 2 + s.length;
        if (totalLength > 127) {
            // We assume that the total length never goes beyond a 1-byte
            // representation
            throw new RuntimeException("Unsupported signature length: " + totalLength);
        }
        writer.put((byte) (totalLength & 0xFF));
        // Write type
        writer.put((byte) 0x02);
        // We assume that the length never goes beyond a 1-byte representation
        writer.put((byte) (r.length & 0xFF));
        // Write bytes
        writer.putBytes(r);
        // Write type
        writer.put((byte) 0x02);
        // We assume that the length never goes beyond a 1-byte representation
        writer.put((byte) (s.length & 0xFF));
        // Write bytes
        writer.putBytes(s);

        return writer.toBytes();
    }
}