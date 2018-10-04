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
import co.coinfinity.infineonandroidapp.ethereum.Erc20Utils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.PREFERENCE_FILENAME;
import static co.coinfinity.AppConstants.TAG;

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
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private InputErrorUtils inputErrorUtils;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private boolean isContractScan;
    private volatile boolean activityStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_erc20_tokens);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        inputErrorUtils = new InputErrorUtils(recipientAddressTxt, amountTxt, gasPriceTxt, gasLimitTxt, contractAddress);

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

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, 0);
        contractAddress.setText(pref.getString("er20ContractAddress", "0xd5ffaa5d81cfe4d4141a11d83d6d7aada39d230e"));
        recipientAddressTxt.setText(pref.getString("er20RecipientAddress", "0xa8e5590D3E1377BAfac30d3D3AB5779A62e0FF28"));

        Handler handler = new Handler();
        new Thread(() -> {
            try {
                while (!activityStopped) {
                    BigInteger transactionPriceBean = Erc20Utils.getErc20Balance(contractAddress.getText().toString(), ethAddress);
                    handler.post(() -> {
                        if (transactionPriceBean != null)
                            currentBalance.setText(String.format(getString(R.string.current_token_balance), transactionPriceBean));
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                    Thread.sleep(3000);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "exception while reading ERC20 Balance", e);
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        activityStopped = true;
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("er20ContractAddress", contractAddress.getText().toString())
                .putString("er20RecipientAddress", recipientAddressTxt.getText().toString())
                .apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }


    @Override
    public void onNewIntent(Intent intent) {
        if (inputErrorUtils.isNoInputError()) {
            this.runOnUiThread(() -> Toast.makeText(SendErc20TokensActivity.this, R.string.hold_card_for_while,
                    Toast.LENGTH_SHORT).show());
            resolveIntent(intent);
        }
    }


    private void resolveIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        final String valueStr = amountTxt.getText().toString();
        final String gasPriceStr = gasPriceTxt.getText().toString();
        final BigDecimal gasPrice = Convert.toWei(gasPriceStr.equals("") ? "0" : gasPriceStr, Convert.Unit.GWEI);
        final String gasLimitStr = gasLimitTxt.getText().toString();
        final BigDecimal gasLimit = Convert.toWei(gasLimitStr.equals("") ? "0" : gasLimitStr, Convert.Unit.WEI);

        try {
            Erc20Utils.sendErc20Tokens(contractAddress.getText().toString(), tagFromIntent, pubKeyString, ethAddress
                    , recipientAddressTxt.getText().toString(), new BigInteger(valueStr.equals("") ? "0" : valueStr), gasPrice.toBigInteger(), gasLimit.toBigInteger(), this);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending ERC20 tokens", e);

        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                if (isContractScan) {
                    contractAddress.setText(data.getStringExtra("SCAN_RESULT"));
                } else {
                    recipientAddressTxt.setText(data.getStringExtra("SCAN_RESULT"));
                }
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

    public void onScanContract(View view) {
        isContractScan = true;
        QrCodeScanner.scanQrCode(this);
    }

    public void onScanRecipient(View view) {
        isContractScan = false;
        QrCodeScanner.scanQrCode(this);
    }
}
