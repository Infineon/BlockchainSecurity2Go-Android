package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Context;
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
import co.coinfinity.infineonandroidapp.ethereum.exceptions.InvalidEthereumAddressException;
import co.coinfinity.infineonandroidapp.ethereum.utils.Erc20Utils;
import co.coinfinity.infineonandroidapp.ethereum.utils.UriUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Activity class used for ER20 Token functionality.
 */
public class SendErc20TokensActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

    @BindView(R.id.recipientAddress)
    TextView recipientAddressTxt;
    @BindView(R.id.amount)
    TextView amountTxt;
    @BindView(R.id.gasPrice)
    TextView gasPriceTxt;
    @BindView(R.id.gasLimit)
    TextView gasLimitTxt;
    @BindView(R.id.contractAddress)
    TextView contractAddress;
    @BindView(R.id.currentBalance)
    TextView currentBalance;
    @BindView(R.id.textViewInfo)
    TextView infoTxt;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.spinner)
    Spinner spinner;

    private InputErrorUtils inputErrorUtils;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private volatile boolean activityPaused = false;

    private UnitSpinnerAdapter spinnerAdapter = new UnitSpinnerAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_erc20_tokens);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinnerAdapter.addSpinnerAdapter(this, spinner);
        inputErrorUtils = new InputErrorUtils(this, recipientAddressTxt, amountTxt, gasPriceTxt, gasLimitTxt, contractAddress);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        pendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        contractAddress.setText(pref.getString(PREF_KEY_ERC20_CONTRACT_ADDRESS, "0xd5ffaa5d81cfe4d4141a11d83d6d7aada39d230e"));
        recipientAddressTxt.setText(pref.getString(PREF_KEY_ERC20_RECIPIENT_ADDRESS, "0xa8e5590D3E1377BAfac30d3D3AB5779A62e0FF28"));
        gasPriceTxt.setText(pref.getString(PREF_KEY_GASPRICE_WEI, "21"));
        gasLimitTxt.setText(pref.getString(PREF_KEY_ERC20_GASLIMIT, "60000"));
        amountTxt.setText(pref.getString(PREF_KEY_ERC20_AMOUNT, "1"));

        new Thread(() -> {
            try {
                while (!activityPaused) {
                    readAndDisplayErc20Balance();
                    TimeUnit.SECONDS.sleep(FIVE_SECONDS);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted exception while reading ERC20 Balance", e);
            }
        }).start();
    }

    /**
     * this method read ERC20 balance via Api request and displays it.
     */
    public void readAndDisplayErc20Balance() {
        BigInteger erc20Balance = new BigInteger("0");
        try {
            Log.d(TAG, "reading ERC20 Balance..");
            erc20Balance = Erc20Utils.getErc20Balance(contractAddress.getText().toString(), ethAddress, UiUtils.getFullNodeUrl(this));
            Log.d(TAG, String.format("got ERC20 Balance: %s", erc20Balance));
        } catch (Exception e) {
            Log.e(TAG, "exception while reading ERC20 Balance", e);
        }
        BigInteger finalErc20Balance = erc20Balance;
        this.runOnUiThread(() -> {
            infoTxt.setText(R.string.hold_card_payment);
            currentBalance.setText(String.format(getString(R.string.current_token_balance), finalErc20Balance));
            progressBar.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        activityPaused = true;
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor
                .putString(PREF_KEY_ERC20_CONTRACT_ADDRESS, contractAddress.getText().toString())
                .putString(PREF_KEY_ERC20_RECIPIENT_ADDRESS, recipientAddressTxt.getText().toString())
                .putString(PREF_KEY_ERC20_GASLIMIT, gasLimitTxt.getText().toString())
                .putString(PREF_KEY_ERC20_AMOUNT, amountTxt.getText().toString())
                .putString(PREF_KEY_GASPRICE_WEI, gasPriceTxt.getText().toString())
                .apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
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
     * will be called after card was hold to back of device.
     *
     * @param intent includes nfc extras
     */
    private void resolveIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        UiUtils.logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        final String valueStr = amountTxt.getText().toString();
        BigDecimal gasPrice = new BigDecimal(gasPriceTxt.getText().toString());
        gasPrice = gasPrice.multiply(spinnerAdapter.getMultiplier());
        final String gasLimitStr = gasLimitTxt.getText().toString();
        final BigDecimal gasLimit = Convert.toWei(gasLimitStr.equals("") ? "0" : gasLimitStr, Convert.Unit.WEI);

        BigDecimal finalGasPrice = gasPrice;
        new Thread(() -> {
            try {
                Log.d(TAG, "Sending ERC20 tokens " + ethAddress);
                final TransactionReceipt receipt = Erc20Utils.sendErc20Tokens(contractAddress.getText().toString(), isoDep, pubKeyString, ethAddress,
                        recipientAddressTxt.getText().toString(), new BigInteger(valueStr.equals("") ? "0" : valueStr), finalGasPrice.toBigInteger(), gasLimit.toBigInteger(), this, UiUtils.getFullNodeUrl(this));
                Log.d(TAG, String.format("ERC20 tokens sent with Hash: %s", receipt.getTransactionHash()));
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending ERC20 tokens", e);
            }
        }).start();

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == 0) {
                    contractAddress.setText(UriUtils.extractEtherAddressFromUri(data.getStringExtra("SCAN_RESULT")));
                } else if (requestCode == 1) {
                    recipientAddressTxt.setText(UriUtils.extractEtherAddressFromUri(data.getStringExtra("SCAN_RESULT")));
                }
            } catch (InvalidEthereumAddressException e) {
                Log.e(TAG, "Exception on reading ethereum address", e);
                showToast(getString(R.string.invalid_ethereum_address), this);
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

    public void onScanContract(View view) {
        QrCodeScanner.scanQrCode(this, 0);
    }

    public void onScanRecipient(View view) {
        QrCodeScanner.scanQrCode(this, 1);
    }
}
