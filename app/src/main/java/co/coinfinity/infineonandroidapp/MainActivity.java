package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.common.ByteUtils;
import co.coinfinity.infineonandroidapp.common.UiUtils;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeGenerator;
import org.web3j.crypto.Keys;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static co.coinfinity.AppConstants.*;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @BindView(R.id.ethAddress)
    TextView ethAddressView;
    @BindView(R.id.balance)
    TextView balance;
    @BindView(R.id.qrCode)
    ImageView qrCodeView;
    @BindView(R.id.holdCard)
    TextView holdCard;
    @BindView(R.id.send)
    Button sendBtn;
    @BindView(R.id.sendErc20)
    Button sendErc20Btn;
    @BindView(R.id.voting)
    Button votingBtn;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String pubKeyString;
    private String ethAddress;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }


    private void resolveIntent(Intent intent) {

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        logTagInfo(tagFromIntent);

        IsoDep isoDep = IsoDep.get(tagFromIntent);

        try {
            pubKeyString = new NfcUtils().getPublicKey(isoDep, CARD_ID);
        } catch (IOException e) {
            Log.e(TAG, "exception while getting public key from card: ", e);
        }
        Log.d(TAG, "pubkey read from card: '" + pubKeyString + "'");
        // use web3j to format this public key as ETH address
        ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));
        ethAddressView.setText(ethAddress);
        Log.d(TAG, "ETH address: " + ethAddress);
        qrCodeView.setImageBitmap(QrCodeGenerator.generateQrCode(ethAddress));
        holdCard.setText(R.string.card_found);

        Handler mHandler = new Handler();
        new Thread(() -> {
            try {
                while (true) {
                    EthBalanceBean balance = EthereumUtils.getBalance(ethAddress);
                    TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApi("0", "0", balance.getEther().toString());
                    if (transactionPriceBean != null) {
                        mHandler.post(() -> {
                            this.balance.setText(String.format("%s%s", balance.toString(),
                                    String.format(Locale.ENGLISH, "\nEuro: %.2f€", transactionPriceBean.getPriceInEuro())));
                            if (!sendBtn.isEnabled()) {
                                sendBtn.setEnabled(true);
                                sendErc20Btn.setEnabled(true);
                                votingBtn.setEnabled(true);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    Thread.sleep(TIMEOUT);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "exception while reading euro price from api: ", e);
            }
        }).start();
    }

    private void logTagInfo(Tag tagFromIntent) {
        Log.d(TAG, "Tag found: " + tagFromIntent.toString());
        Log.d(TAG, "Id: " + ByteUtils.bytesToHex(tagFromIntent.getId()));
        for (String tech : tagFromIntent.getTechList()) {
            Log.d(TAG, "Tech: " + tech);
        }
    }

    public void onSend(View view) {
        Intent intent = new Intent(this, SendTransactionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onSendErc20(View view) {
        Intent intent = new Intent(this, SendErc20TokensActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onVoting(View view) {
        Intent intent = new Intent(this, VotingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
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
