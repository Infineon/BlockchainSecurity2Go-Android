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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.common.EthereumUtils;
import co.coinfinity.infineonandroidapp.common.QrCodeGenerator;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView text;
    private TextView balance;
    private String balanceText;

    private ImageView qrCodeView;

//    byte[] SELECT = {
//            (byte) 0x00, // CLA Class
//            (byte) 0xA4, // INS Instruction
//            (byte) 0x04, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x0D, // Length
//            (byte) 0xD2,
//            0x76,0x00,0x00,0x04,0x15,0x02,0x00,0x01,0x00,0x00,0x00,0x01 // AID
//    };
//
//    //        reflector
//    final byte[] REFLECTOR = {
//            (byte) 0x80, // CLA Class
//            (byte) 0xFF, // INS Instruction
//            (byte) 0x00, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x01, // Length
//            (byte) 0xFF,
//            (byte) 0x00,
//    };
//
//    //        get version
//    final byte[] GET_VERSION = {
//            (byte) 0x00, // CLA Class
//            (byte) 0x88, // INS Instruction
//            (byte) 0x00, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x00, // Length
//    };
//    //        create Key
//    final byte[] CREATE_KEY = {
//            (byte) 0x00, // CLA Class
//            (byte) 0x02, // INS Instruction
//            (byte) 0x01, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x00, // Length
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        balance = (TextView) findViewById(R.id.balance);

        qrCodeView = (ImageView) findViewById(R.id.qrCode);

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
        Log.d("TAG", "Tag found: " + tagFromIntent.toString());
        Log.d("TAG", "Id: " + EthereumUtils.bytesToHex(tagFromIntent.getId()));
        for (String tech: tagFromIntent.getTechList()) {
            Log.d("TAG", "Tech: " + tech);
        }

        String pubKeyString = null;
        IsoDep isoDep = IsoDep.get(tagFromIntent);
        try {
            isoDep.connect();
            pubKeyString = EthereumUtils.getPublicKey(isoDep, 0x01);
            isoDep.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // use web3j to format this public key as ETH address
        String ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));
        text.setText(text.getText()+" "+ethAddress);
        Log.i("info",ethAddress);
        qrCodeView.setImageBitmap(QrCodeGenerator.generateQrCode(ethAddress));


        Handler mHandler = new Handler();

        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    Thread.sleep(1000);
                    balanceText = EthereumUtils.getBalance(ethAddress);
                    mHandler.post(() -> balance.setText(balanceText));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }




}
