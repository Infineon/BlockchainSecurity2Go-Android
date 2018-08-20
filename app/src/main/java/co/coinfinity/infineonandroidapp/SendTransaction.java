package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import co.coinfinity.infineonandroidapp.common.HttpUtils;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static co.coinfinity.AppConstants.TAG;

public class SendTransaction extends AppCompatActivity {

    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;

    private TextView priceInEuroTxt;

    private String pubKeyString;
    private String ethAddress;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        recipientAddressTxt = (TextView) findViewById(R.id.recipientAddress);
        amountTxt = (TextView) findViewById(R.id.amount);
        gasPriceTxt = (TextView) findViewById(R.id.gasPrice);
        gasLimitTxt = (TextView) findViewById(R.id.gasLimit);

        priceInEuroTxt = (TextView) findViewById(R.id.priceInEuro);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    mHandler.post(() -> {
                        readPriceFromApi();
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "exception while reading price info from API in thread: ", e);
            }
        });

        thread.start();
    }

    private void readPriceFromApi() {
        HttpUtils.get("https://coinfinity.co/price/XBTEUR", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "XBTEUR Price: " + response);
                try {
                    JSONObject serverResp = new JSONObject(response.toString());

                    if (!gasPriceTxt.getText().toString().equals("") && !gasLimitTxt.getText().toString().equals("")) {
                        BigDecimal gasPrice = new BigDecimal(gasPriceTxt.getText().toString());
                        BigDecimal gasLimit = new BigDecimal(gasLimitTxt.getText().toString());
                        final BigDecimal weiGasPrice = Convert.toWei(gasPrice.multiply(gasLimit), Convert.Unit.GWEI);
                        final BigDecimal ethGasPrice = Convert.fromWei(weiGasPrice, Convert.Unit.ETHER);

                        String priceStr = String.format("Price: %.2f€ \n Tx Fee: %.2f€", serverResp.getDouble("ask") * Double.parseDouble(amountTxt.getText().toString()), ethGasPrice.floatValue() * serverResp.getDouble("ask"));
                        priceInEuroTxt.setText(priceStr);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "exception while reading price info from API: ", e);
                }
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Thread thread = new Thread(() -> {
            final BigDecimal value = Convert.toWei(amountTxt.getText().toString(), Convert.Unit.ETHER);
            final BigDecimal gasPrice = Convert.toWei(gasPriceTxt.getText().toString(), Convert.Unit.GWEI);
            final BigDecimal gasLimit = Convert.toWei(gasLimitTxt.getText().toString(), Convert.Unit.WEI);
            EthereumUtils.sendTransaction(gasPrice.toBigInteger(), gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(), value.toBigInteger(), tagFromIntent, pubKeyString);
        });

        thread.start();
        finish();
    }

    public void scanQrCode(View view) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                recipientAddressTxt.setText(data.getStringExtra("SCAN_RESULT"));
            }
            if (resultCode == RESULT_CANCELED) {
                //handle cancel
            }
        }
    }
}
