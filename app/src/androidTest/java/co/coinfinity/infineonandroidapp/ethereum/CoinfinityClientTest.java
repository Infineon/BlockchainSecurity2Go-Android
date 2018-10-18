package co.coinfinity.infineonandroidapp.ethereum;

import android.support.test.runner.AndroidJUnit4;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CoinfinityClientTest {

    private CoinfinityClient coinfinityClient;

    @Test
    public void testIfClientGetsEuroPriceFromApi() throws Exception {
        coinfinityClient = new CoinfinityClient();
        final TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApiSync("10", "1000", "10");

        assertThat(transactionPriceBean.getPriceInEuro(),
                greaterThan((double) 0));
        assertThat(transactionPriceBean.getTxFeeInEuro(),
                greaterThan((double) 0));
    }
}
