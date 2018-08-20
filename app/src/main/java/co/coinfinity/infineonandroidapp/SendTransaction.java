package co.coinfinity.infineonandroidapp;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import co.coinfinity.infineonandroidapp.ethereum.EthereumUtils;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

public class SendTransaction extends AppCompatActivity {

    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;

    private String pubKeyString;
    private String ethAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recipientAddressTxt = (TextView) findViewById(R.id.recipientAddress);
        amountTxt = (TextView) findViewById(R.id.amount);
        gasPriceTxt = (TextView) findViewById(R.id.gasPrice);
        gasLimitTxt = (TextView) findViewById(R.id.gasLimit);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onSend(View view) {

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        Tag tagFromIntent = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Thread thread = new Thread(() -> {
            final BigDecimal value = Convert.toWei(amountTxt.getText().toString(), Convert.Unit.ETHER);
            final BigDecimal gasPrice = Convert.toWei(gasPriceTxt.getText().toString(), Convert.Unit.GWEI);
            final BigDecimal gasLimit = Convert.toWei(gasLimitTxt.getText().toString(), Convert.Unit.WEI);
            EthereumUtils.sendTransaction(gasPrice.toBigInteger(), gasLimit.toBigInteger(), ethAddress, recipientAddressTxt.getText().toString(), value.toBigInteger(), tagFromIntent, pubKeyString);
        });

        thread.start();
        finish();
    }
}
