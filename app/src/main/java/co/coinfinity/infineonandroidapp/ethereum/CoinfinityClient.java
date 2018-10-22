package co.coinfinity.infineonandroidapp.ethereum;

import android.util.Log;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static co.coinfinity.AppConstants.*;

/**
 * Simple HTTP client for fetching price data.
 */
public class CoinfinityClient extends JsonHttpResponseHandler {

    private TransactionPriceBean transactionPriceBean;
    private String gasPriceStr;
    private String gasLimitStr;
    private String etherAmount;

    /**
     * this method reads the euro price from coinfinity api and calculates the corresponding euro amount of an ether amount, gas price and gas limit.
     *
     * @param gasPriceStr gas price
     * @param gasLimitStr gas limit
     * @param etherAmount ether
     * @return transaction bean
     */
    public TransactionPriceBean readEuroPriceFromApiSync(String gasPriceStr, String gasLimitStr, String etherAmount) throws Exception {
        this.gasPriceStr = gasPriceStr;
        this.gasLimitStr = gasLimitStr;
        this.etherAmount = etherAmount;

        HttpUtils.get(HTTPS_COINFINITY_CO_PRICE_XBTEUR, null, this);

        return transactionPriceBean;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        try {
            Log.d(TAG, String.format("Response from ETH/EUR API request. HTTP: %d", statusCode));
            JSONObject serverResp = new JSONObject(response.toString());

            if (!gasPriceStr.equals("") && !gasLimitStr.equals("") && !etherAmount.equals("")) {
                BigDecimal gasPrice = new BigDecimal(gasPriceStr);
                BigDecimal gasLimit = new BigDecimal(gasLimitStr);
                final BigDecimal weiGasPrice = Convert.toWei(gasPrice.multiply(gasLimit), Convert.Unit.GWEI);
                final BigDecimal ethGasPrice = Convert.fromWei(weiGasPrice, Convert.Unit.ETHER);

                transactionPriceBean = new TransactionPriceBean(serverResp.getDouble(ASK) * Double.parseDouble(etherAmount), ethGasPrice.floatValue() * serverResp.getDouble(ASK));
            }
        } catch (JSONException e) {
            Log.e(TAG, "exception while reading price info from API: ", e);
        }
    }
}
