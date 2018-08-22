package co.coinfinity.infineonandroidapp;

import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

import static co.coinfinity.infineonandroidapp.ethereum.EthereumUtils.getNextNonce;

public class NfcUtilsMock {
    @Test
    public void sendWorkingTransaction() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException, ExecutionException, InterruptedException {
//        String fileName = WalletUtils.generateLightNewWalletFile(
//                "your password",
//                new File("/home/daniel/IdeaProjects/infineon-android-demo/app/src/test/java/co/coinfinity/infineonandroidapp/test/test.json"));

        Credentials credentials = WalletUtils.loadCredentials(
                "your password",
                "/home/daniel/IdeaProjects/infineon-android-demo/app/src/test/java/co/coinfinity/infineonandroidapp/test/UTC--2018-08-17T14-19-20.531--b8560f44ac3bc78645a8cef82e012d85bd18878f.json");
        System.out.println(credentials.getAddress());
        System.out.println(credentials.getEcKeyPair().getPrivateKey());
        System.out.println(credentials.getEcKeyPair().getPublicKey());

        final BigDecimal value = Convert.toWei("0.0003", Convert.Unit.ETHER);
        final BigDecimal gasPrice = Convert.toWei("1", Convert.Unit.GWEI);
        final BigDecimal gasLimit = Convert.toWei("121000", Convert.Unit.WEI);

        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                getNextNonce(web3, credentials.getAddress()), gasPrice.toBigInteger(), gasLimit.toBigInteger(), "0xe09eD054044763E03e0e59460F773F69DB9A333A", value.toBigInteger());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        System.out.println(hexValue);

        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println(transactionHash);
        if (ethSendTransaction.getError() != null)
            System.out.println(ethSendTransaction.getError().getMessage());
        System.out.println(ethSendTransaction.getResult());
    }

    @Test
    public void sendWorkingTransaction2() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException, ExecutionException, InterruptedException {
//        String fileName = WalletUtils.generateLightNewWalletFile(
//                "your password",
//                new File("/home/daniel/IdeaProjects/infineon-android-demo/app/src/test/java/co/coinfinity/infineonandroidapp/test/test.json"));

        Credentials credentials = WalletUtils.loadCredentials(
                "your password",
                "/home/daniel/IdeaProjects/infineon-android-demo/app/src/test/java/co/coinfinity/infineonandroidapp/test/UTC--2018-08-17T14-19-20.531--b8560f44ac3bc78645a8cef82e012d85bd18878f.json");
        System.out.println(credentials.getAddress());
        System.out.println(credentials.getEcKeyPair().getPrivateKey());
        System.out.println(credentials.getEcKeyPair().getPublicKey());

        final BigDecimal value = Convert.toWei("0.0003", Convert.Unit.ETHER);
        final BigDecimal gasPrice = Convert.toWei("1", Convert.Unit.GWEI);
        final BigDecimal gasLimit = Convert.toWei("121000", Convert.Unit.WEI);

        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                getNextNonce(web3, credentials.getAddress()), gasPrice.toBigInteger(), gasLimit.toBigInteger(), "0xe09eD054044763E03e0e59460F773F69DB9A333A", value.toBigInteger());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        System.out.println("RESULT: " + hexValue);

//        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
//        String transactionHash = ethSendTransaction.getTransactionHash();
//        System.out.println(transactionHash);
//        if(ethSendTransaction.getError() != null)
//            System.out.println(ethSendTransaction.getError().getMessage());
//        System.out.println(ethSendTransaction.getResult());
    }

}
