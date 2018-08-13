package co.coinfinity.infineonandroidapp;

import org.junit.Test;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3Sha3;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.rx.Web3jRx;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void test() {
        String keccakHash = Hash.sha3("0x000000000000000000000000000000000000000000000000000000000000000000");
        String ethAddress = "0x" + keccakHash.substring(26);
//        Poloniex Wallet
//        ethAddress = "0x32Be343B94f860124dC4fEe278FDCBD38C102D88";
        System.out.println(keccakHash);
        System.out.println(ethAddress);

        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService("https://mainnet.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));  // defaults to http://localhost:8545/

        // send asynchronous requests to get balance
        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = web3
                    .ethGetBalance(ethAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger wei = ethGetBalance.getBalance();
        BigDecimal ether = Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
        System.out.println(wei + " Wei");
        System.out.println(ether + " Ether");
    }
}