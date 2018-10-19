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
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.ethereum.exceptions.InvalidEthereumAddressException;
import co.coinfinity.infineonandroidapp.ethereum.utils.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.utils.UriUtils;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Activity class used for Ethereum functionality.
 */
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

    private UnitSpinnerAdapter spinnerAdapter = new UnitSpinnerAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinnerAdapter.addSpinnerAdapter(this, spinner);
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
        String savedGasPriceWei = mPrefs.getString(PREF_KEY_GASPRICE_WEI, "21");
        gasPriceTxt.setText(savedGasPriceWei);
        String savedGasLimit = mPrefs.getString(PREF_KEY_GASLIMIT_SEND_ETH, "21000");
        gasLimitTxt.setText(savedGasLimit);

        new Thread(() -> {
            try {
                while (!activityPaused) {
                    Log.d(TAG, "reading EUR/ETH price..");
                    TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApiSync(gasPriceTxt.getText().toString(), gasLimitTxt.getText().toString(), amountTxt.getText().toString());
                    Log.d(TAG, "reading EUR/ETH price finished: " + transactionPriceBean);
                    this.runOnUiThread(() -> {
                        if (transactionPriceBean != null) {
                            priceInEuroTxt.setText(transactionPriceBean.toString());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    TimeUnit.SECONDS.sleep(TEN_SECONDS);
                }
            } catch (Exception e) {
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
            showToast(getString(R.string.hold_card_for_while), this);
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

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        UiUtils.logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        new Thread(() -> sendTransactionAndShowFeedback(isoDep)).start();
        finish();
    }

    /**
     * reads data needed for transaction, sends an Ethereum transaction and shows feedback on UI.
     *
     * @param isoDep
     */
    private void sendTransactionAndShowFeedback(IsoDep isoDep) {
        final String valueStr = amountTxt.getText().toString();
        final BigDecimal value = Convert.toWei(valueStr.equals("") ? "0" : valueStr, Convert.Unit.ETHER);
        BigDecimal gasPrice = new BigDecimal(gasPriceTxt.getText().toString());
        gasPrice = gasPrice.multiply(spinnerAdapter.getMultiplier());
        final String gasLimitStr = gasLimitTxt.getText().toString();
        final BigDecimal gasLimit = new BigDecimal(gasLimitStr.equals("") ? "0" : gasLimitStr);

        EthSendTransaction response = null;
        try {
            Log.d(TAG, "sending ETH transaction..");
            response = EthereumUtils.sendTransaction(gasPrice.toBigInteger(),
                    gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(),
                    value.toBigInteger(), isoDep, pubKeyString, "");
            Log.d(TAG, String.format("sending ETH transaction finished with Hash: %s", response.getTransactionHash()));
        } catch (NfcCardException e) {
            showToast(getString(R.string.operation_not_supported), this);
            return;
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ether transaction", e);
            showToast(String.format("Could not send transaction: %s", e.getMessage()), this);
            return;
        }

        if (response.getError() != null) {
            showToast(response.getError().getMessage(), this);
        } else {
            showToast(getString(R.string.send_success), this);
        }
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == 0)
                    recipientAddressTxt.setText(UriUtils.extractEtherAddressFromUri(data.getStringExtra("SCAN_RESULT")));
            } catch (InvalidEthereumAddressException e) {
                Log.e(TAG, "Exception on reading ethereum address", e);
                showToast("Invalid Ethereum address", this);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "QR Code scanning canceled.");
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
