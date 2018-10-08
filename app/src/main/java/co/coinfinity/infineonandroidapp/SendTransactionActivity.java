package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
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
    @BindView(R.id.spinner)
    Spinner spinner;

    private InputErrorUtils inputErrorUtils;

    private String pubKeyString;
    private String ethAddress;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();
    private volatile boolean activityPaused = false;

    private UnitSpinnerAdapter spinnerUtils = new UnitSpinnerAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinnerUtils.addSpinnerAdapter(this, spinner);
        inputErrorUtils = new InputErrorUtils(this, recipientAddressTxt, amountTxt, gasPriceTxt, gasLimitTxt);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        pendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        SharedPreferences mPrefs = getSharedPreferences(PREFERENCE_FILENAME, 0);
        String savedRecipientAddressTxt = mPrefs.getString(PREF_KEY_RECIPIENT_ADDRESS, "");
        recipientAddressTxt.setText(savedRecipientAddressTxt);
        String savedGasPriceWei  = mPrefs.getString(PREF_KEY_GASPRICE_WEI, "21");
        gasPriceTxt.setText(savedGasPriceWei);
        String savedGasLimit  = mPrefs.getString(PREF_KEY_GASLIMIT_SEND_ETH, "21000");
        gasLimitTxt.setText(savedGasLimit);

        new Thread(() -> {
            try {
                while (!activityPaused) {
                    TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApi(gasPriceTxt.getText().toString(), gasLimitTxt.getText().toString(), amountTxt.getText().toString());
                    this.runOnUiThread(() -> {
                        if (transactionPriceBean != null) {
                            priceInEuroTxt.setText(transactionPriceBean.toString());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    Thread.sleep(SLEEP_BETWEEN_LOOPS_MILLIS);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Exception while reading price info from API in thread", e);
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        activityPaused = true;
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);

        SharedPreferences mPrefs = getSharedPreferences(PREFERENCE_FILENAME, 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_KEY_RECIPIENT_ADDRESS, recipientAddressTxt.getText().toString()).apply();
        mEditor.putString(PREF_KEY_GASPRICE_WEI, gasPriceTxt.getText().toString()).apply();
        mEditor.putString(PREF_KEY_GASLIMIT_SEND_ETH, gasLimitTxt.getText().toString()).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        activityPaused = false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (inputErrorUtils.isNoInputError()) {
            this.runOnUiThread(() -> Toast.makeText(
                    SendTransactionActivity.this, R.string.hold_card_for_while,
                    Toast.LENGTH_LONG).show());
            resolveIntent(intent);
        }
    }

    /**
     * will be called after card was hold to back of device
     *
     * @param intent includes nfc extras
     */
    private void resolveIntent(Intent intent) {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // TODO check if IsoDeps
        IsoDep isoDep = IsoDep.get(tagFromIntent);

        new Thread(() -> {
            final String valueStr = amountTxt.getText().toString();
            final BigDecimal value = Convert.toWei(valueStr.equals("") ? "0" : valueStr, Convert.Unit.ETHER);
            BigDecimal gasPrice = new BigDecimal(gasPriceTxt.getText().toString());
            gasPrice = gasPrice.multiply(spinnerUtils.getMultiplier());
            final String gasLimitStr = gasLimitTxt.getText().toString();
            final BigDecimal gasLimit = new BigDecimal(gasLimitStr.equals("") ? "0" : gasLimitStr);

            EthSendTransaction response = null;
            try {
                response = EthereumUtils.sendTransaction(gasPrice.toBigInteger(),
                        gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(),
                        value.toBigInteger(), isoDep, pubKeyString, "");
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending ether transaction", e);
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this,
                        String.format("Could not send transaction: %s", e.getMessage()), Toast.LENGTH_LONG).show());
                return;
            }

            if (response != null && response.getError() != null) {
                EthSendTransaction finalResponse = response;
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, finalResponse.getError().getMessage(),
                        Toast.LENGTH_LONG).show());
            } else {
                this.runOnUiThread(() -> Toast.makeText(SendTransactionActivity.this, R.string.send_success, Toast.LENGTH_SHORT).show());
            }
        }).start();
        finish();
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this, 0);
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
        return UiUtils.handleOptionItemSelected(this, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
