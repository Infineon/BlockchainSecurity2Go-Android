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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.common.InputErrorUtils;
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
import static co.coinfinity.AppConstants.*;

public class SendTransactionActivity extends AppCompatActivity {

    @BindView(R.id.recipientAddress)
    TextView recipientAddressTxt;
    @BindView(R.id.amount)
    TextView amountTxt;
    @BindView(R.id.gasPrice)
    TextView gasPriceTxt;
    @BindView(R.id.gasLimit)
    TextView gasLimitTxt;
    @BindView(R.id.priceInEuro)
    TextView priceInEuroTxt;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private InputErrorUtils inputErrorUtils;

    private String pubKeyString;
    private String ethAddress;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();
    private volatile boolean activityStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        inputErrorUtils = new InputErrorUtils(recipientAddressTxt, amountTxt, gasPriceTxt, gasLimitTxt);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        pendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        SharedPreferences mPrefs = getSharedPreferences(PREFERENCE_FILENAME, 0);
        String savedRecipientAddressTxt = mPrefs.getString("recipientAddressTxt", "");
        recipientAddressTxt.setText(savedRecipientAddressTxt);

        Handler handler = new Handler();
        new Thread(() -> {
            try {
                while (!activityStopped) {
                    TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApi(gasPriceTxt.getText().toString(), gasLimitTxt.getText().toString(), amountTxt.getText().toString());
                    handler.post(() -> {
                        if (transactionPriceBean != null) {
                            priceInEuroTxt.setText(transactionPriceBean.toString());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    Thread.sleep(TIMEOUT);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "exception while reading price info from API in thread", e);
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        activityStopped = true;
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);

        SharedPreferences mPrefs = getSharedPreferences(PREFERENCE_FILENAME, 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("recipientAddressTxt", recipientAddressTxt.getText().toString()).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (inputErrorUtils.isNoInputError()) {
            this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, R.string.hold_card_for_while,
                    Toast.LENGTH_SHORT).show());
            resolveIntent(intent);
        }
    }



    private void resolveIntent(Intent intent) {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        new Thread(() -> {
            final String valueStr = amountTxt.getText().toString();
            final BigDecimal value = Convert.toWei(valueStr.equals("") ? "0" : valueStr, Convert.Unit.ETHER);
            final String gasPriceStr = gasPriceTxt.getText().toString();
            final BigDecimal gasPrice = Convert.toWei(gasPriceStr.equals("") ? "0" : gasPriceStr, Convert.Unit.GWEI);
            final String gasLimitStr = gasLimitTxt.getText().toString();
            final BigDecimal gasLimit = Convert.toWei(gasLimitStr.equals("") ? "0" : gasLimitStr, Convert.Unit.WEI);

            EthSendTransaction response = null;
            try {
                response = EthereumUtils.sendTransaction(gasPrice.toBigInteger(), gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(), value.toBigInteger(), tagFromIntent, pubKeyString, new NfcUtils(), "");
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending ether transaction", e);
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, "Could not send transaction!", Toast.LENGTH_SHORT).show());
                return;
            }

            if (response != null && response.getError() != null) {
                EthSendTransaction finalResponse = response;
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, finalResponse.getError().getMessage(),
                        Toast.LENGTH_SHORT).show());
            } else {
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, R.string.send_success, Toast.LENGTH_SHORT).show());
            }
        }).start();
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
