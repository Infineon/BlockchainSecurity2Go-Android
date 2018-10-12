package co.coinfinity.infineonandroidapp.ethereum.utils;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class Erc20UtilsTest {

    @Test
    public void testSendErc20Tokens() {

//        // sign & send our transaction
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//
//        //TODO how to sign without card?
////        IsoDep isoDep = IsoDep.get(new Tag());
//        Erc20Utils.sendErc20Tokens("0xd5ffaa5d81cfe4d4141a11d83d6d7aada39d230e",isoDep,"0xbef897065c24f50b13be14df66d8ffbcd117fcde", "0x8720af26d1810ae8fca995002ad56175e4c97b23","0x8720af26d1810ae8fca995002ad56175e4c97b23",new BigInteger("100"),new BigInteger("50"),new BigInteger("10000"),null);
    }

    @Test
    public void testGetErc20Balance() throws Exception {
        final BigInteger erc20Balance = Erc20Utils.getErc20Balance("0xd5ffaa5d81cfe4d4141a11d83d6d7aada39d230e", "0xbef897065c24f50b13be14df66d8ffbcd117fcde");

        System.out.printf("Token Balance: %s%n", erc20Balance);
        assertThat(erc20Balance.intValue(),
                greaterThan(0));
    }
}