package co.coinfinity.infineonandroidapp.ethereum;

import android.util.Log;
import co.coinfinity.infineonandroidapp.common.HttpUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static co.coinfinity.AppConstants.TAG;

public class CoinfinityClient extends JsonHttpResponseHandler {

    private static final String ETHEUR = "ETHEUR";
    private static final String HTTPS_COINFINITY_CO_PRICE_XBTEUR = "https://coinfinity.co/price/" + ETHEUR;
    private static final String ASK = "ask";

    private TransactionPriceBean transactionPriceBean;
    private String gasPriceStr;
    private String gasLimitStr;
    private String amount;


    public TransactionPriceBean readEthPriceFromApi(String gasPriceStr, String gasLimitStr, String amount) {
        this.gasPriceStr = gasPriceStr;
        this.gasLimitStr = gasLimitStr;
        this.amount = amount;

        HttpUtils.get(HTTPS_COINFINITY_CO_PRICE_XBTEUR, null, this);

        return transactionPriceBean;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d(TAG, "XBTEUR Price: " + response);
        try {
            JSONObject serverResp = new JSONObject(response.toString());

            if (!gasPriceStr.equals("") && !gasLimitStr.equals("") && !amount.equals("")) {
                BigDecimal gasPrice = new BigDecimal(gasPriceStr);
                BigDecimal gasLimit = new BigDecimal(gasLimitStr);
                final BigDecimal weiGasPrice = Convert.toWei(gasPrice.multiply(gasLimit), Convert.Unit.GWEI);
                final BigDecimal ethGasPrice = Convert.fromWei(weiGasPrice, Convert.Unit.ETHER);

                transactionPriceBean = new TransactionPriceBean(serverResp.getDouble(ASK) * Double.parseDouble(amount), ethGasPrice.floatValue() * serverResp.getDouble(ASK));
            }
        } catch (JSONException e) {
            Log.e(TAG, "exception while reading price info from API: ", e);
        }
    }
}
