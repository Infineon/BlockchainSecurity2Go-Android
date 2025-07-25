package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.utils.UiUtils;


public class BrandProtection extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.productDetails)
    Button productDetails;
    @BindView(R.id.verifyProduct)
    Button verifyProduct;
    private String pubKeyString,ethAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_protection);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pubKeyString = bundle.getString("pubKey");
            ethAddress = bundle.getString("ethAddress");
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

    /**
     * send public key and ether address as a bundle to
     * BrandProtection_AddDetails class
     */

    public  void onAddDetails(View view){
        Intent intent=new Intent(this, BrandProtection_AddDetails.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Button to verify the product
     */

    public void onVerifyProduct(View view) {
        Intent intent=new Intent(this, BrandProtection_VerifyProduct.class);
        startActivity(intent);
    }
}
