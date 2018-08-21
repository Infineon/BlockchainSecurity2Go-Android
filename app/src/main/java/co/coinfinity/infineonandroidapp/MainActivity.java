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
import co.coinfinity.infineonandroidapp.common.Utils;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.nfc.NfcUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeGenerator;
import org.web3j.crypto.Keys;

import java.util.Locale;

import static co.coinfinity.AppConstants.TAG;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private TextView holdCard;
    private TextView ethAddressView;
    private TextView balance;
    private ImageView qrCodeView;
    private Button sendBtn;
    private ProgressBar progressBar;

    private String pubKeyString;
    private String ethAddress;
    private String balanceText;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ethAddressView = (TextView) findViewById(R.id.ethAddress);
        balance = (TextView) findViewById(R.id.balance);
        qrCodeView = (ImageView) findViewById(R.id.qrCode);
        sendBtn = (Button) findViewById(R.id.send);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
        Log.d(TAG, "Tag found: " + tagFromIntent.toString());
        Log.d(TAG, "Id: " + Utils.bytesToHex(tagFromIntent.getId()));
        for (String tech : tagFromIntent.getTechList()) {
            Log.d(TAG, "Tech: " + tech);
        }

        IsoDep isoDep = IsoDep.get(tagFromIntent);
        try {
            isoDep.connect();
            pubKeyString = NfcUtils.getPublicKey(isoDep, 0x00);
            Log.d(TAG, "pubkey read from card: '" + pubKeyString + "'");
            isoDep.close();
        } catch (Exception e) {
            Log.e(TAG, "exception while reading pubkey: ", e);
        }

        // use web3j to format this public key as ETH address
        ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));
        ethAddressView.setText(ethAddress);
        Log.d(TAG, "ETH: address" + ethAddress);
        qrCodeView.setImageBitmap(QrCodeGenerator.generateQrCode(ethAddress));

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    final EthBalanceBean balance = EthereumUtils.getBalance(ethAddress);
                    balanceText = balance.toString();
                    final TransactionPriceBean transactionPriceBean = coinfinityClient.readEthPriceFromApi("0", "0", balance.getEther().toString());
                    if (transactionPriceBean != null) {
                        balanceText += String.format(Locale.ENGLISH, "\nEuro: %.2f€", transactionPriceBean.getPriceInEuro());
                    }
                    mHandler.post(() -> {
                        this.balance.setText(balanceText);
                        if (!sendBtn.isEnabled()) {
                            sendBtn.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    public void onSend(View view) {
        Intent intent = new Intent(this, SendTransaction.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
