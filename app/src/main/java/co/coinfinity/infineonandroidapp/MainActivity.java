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
import android.util.Log;
import android.view.View;
import android.widget.*;
import co.coinfinity.infineonandroidapp.common.ByteUtils;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeGenerator;
import org.web3j.crypto.Keys;

import java.util.Locale;

import static co.coinfinity.AppConstants.CARD_ID;
import static co.coinfinity.AppConstants.TAG;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private TextView ethAddressView;
    private TextView balance;
    private ImageView qrCodeView;
    private TextView holdCard;
    private Button sendBtn;
    private Button sendErc20Btn;
    private ProgressBar progressBar;

    private String pubKeyString;
    private String ethAddress;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ethAddressView = findViewById(R.id.ethAddress);
        balance = findViewById(R.id.balance);
        qrCodeView = findViewById(R.id.qrCode);
        sendBtn = findViewById(R.id.send);
        sendErc20Btn = findViewById(R.id.sendErc20);
        progressBar = findViewById(R.id.progressBar);
        holdCard = findViewById(R.id.holdCard);

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

        pubKeyString = new NfcUtils().getPublicKey(isoDep, CARD_ID);
        Log.d(TAG, "pubkey read from card: '" + pubKeyString + "'");
        // use web3j to format this public key as ETH address
        ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));
        ethAddressView.setText(ethAddress);
        Log.d(TAG, "ETH address: " + ethAddress);
        qrCodeView.setImageBitmap(QrCodeGenerator.generateQrCode(ethAddress));
        holdCard.setText(R.string.card_found);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
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
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "exception while reading euro price from api: ", e);
            }
        });

        thread.start();
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
}
