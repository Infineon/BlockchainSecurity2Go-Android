package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.common.UiUtils;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.TAG;

public class SendTransactionActivity extends AppCompatActivity {

    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;

    private TextView priceInEuroTxt;

    private String pubKeyString;
    private String ethAddress;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        recipientAddressTxt = findViewById(R.id.recipientAddress);
        amountTxt = findViewById(R.id.amount);
        gasPriceTxt = findViewById(R.id.gasPrice);
        gasLimitTxt = findViewById(R.id.gasLimit);
        priceInEuroTxt = findViewById(R.id.priceInEuro);

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        String savedRecipientAddressTxt = mPrefs.getString("recipientAddressTxt", "");
        recipientAddressTxt.setText(savedRecipientAddressTxt);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    mHandler.post(() -> {
                        TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApi(gasPriceTxt.getText().toString(), gasLimitTxt.getText().toString(), amountTxt.getText().toString());
                        if (transactionPriceBean != null)
                            priceInEuroTxt.setText(transactionPriceBean.toString());
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "exception while reading price info from API in thread: ", e);
            }
        });

        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("recipientAddressTxt", recipientAddressTxt.getText().toString()).apply();
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
            final String valueStr = amountTxt.getText().toString();
            final BigDecimal value = Convert.toWei(valueStr.equals("") ? "0" : valueStr, Convert.Unit.ETHER);
            final String gasPriceStr = gasPriceTxt.getText().toString();
            final BigDecimal gasPrice = Convert.toWei(gasPriceStr.equals("") ? "0" : gasPriceStr, Convert.Unit.GWEI);
            final String gasLimitStr = gasLimitTxt.getText().toString();
            final BigDecimal gasLimit = Convert.toWei(gasLimitStr.equals("") ? "0" : gasLimitStr, Convert.Unit.WEI);
            final EthSendTransaction response = EthereumUtils.sendTransaction(gasPrice.toBigInteger(), gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(), value.toBigInteger(), tagFromIntent, pubKeyString, new NfcUtils(), "");

            if (response.getError() != null) {
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, response.getError().getMessage(),
                        Toast.LENGTH_LONG).show());
            } else {
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, R.string.send_success, Toast.LENGTH_LONG).show());
            }
        });

        thread.start();
        finish();
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return UiUtils.handleOptionITemSelected(this, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
