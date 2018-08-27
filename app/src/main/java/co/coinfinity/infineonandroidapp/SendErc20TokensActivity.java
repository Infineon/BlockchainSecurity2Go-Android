package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.ethereum.Erc20Utils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.TAG;

public class SendErc20TokensActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;
    private TextView contractAddress;
    private TextView currentBalance;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_erc20_tokens);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        contractAddress = findViewById(R.id.contractAddress);

        currentBalance = findViewById(R.id.currentBalance);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    mHandler.post(() -> {
                        BigInteger transactionPriceBean = null;
                        try {
                            transactionPriceBean = Erc20Utils.getErc20Balance(contractAddress.getText().toString(), ethAddress);
                        } catch (Exception e) {
                            Log.e(TAG, "exception while reading ERC20 Balance: ", e);
                        }
                        if (transactionPriceBean != null)
                            currentBalance.setText(String.format("Current Token Balance: %s", transactionPriceBean));
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "exception while reading ERC20 Balance: ", e);
            }
        });

        thread.start();
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
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Thread thread = new Thread(() -> {
            final BigDecimal gasPrice = Convert.toWei(gasPriceTxt.getText().toString(), Convert.Unit.GWEI);
            final BigDecimal gasLimit = Convert.toWei(gasLimitTxt.getText().toString(), Convert.Unit.WEI);
            try {
                final TransactionReceipt response = Erc20Utils.sendErc20Tokens(contractAddress.getText().toString(), tagFromIntent, pubKeyString, ethAddress
                        , recipientAddressTxt.getText().toString(), new BigInteger(amountTxt.getText().toString()), gasPrice.toBigInteger(), gasLimit.toBigInteger());

                if (response != null) {
                    this.runOnUiThread(() -> Toast.makeText(SendErc20TokensActivity.this, response.getStatus(),
                            Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "exception while sending ERC20 tokens: ", e);
            }
        });

        thread.start();
        finish();
    }

}
