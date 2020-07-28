package co.coinfinity.infineonandroidapp;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.web3j.crypto.Keys;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.utils.ProductDetailUtils;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;
import co.coinfinity.infineonandroidapp.utils.UiUtils;

import static co.coinfinity.AppConstants.DEFAULT_PRODUCT_DETAIL_ADDRESS_TESTNET;
import static co.coinfinity.AppConstants.GASLIMIT;
import static co.coinfinity.AppConstants.GASPRICE;
import static co.coinfinity.AppConstants.KEY_INDEX_OF_CARD;
import static co.coinfinity.AppConstants.PREFERENCE_FILENAME;

import static co.coinfinity.AppConstants.PREF_KEY_BRANDPROTECTION_GASLIMIT;
import static co.coinfinity.AppConstants.PREF_KEY_GASPRICE_WEI;

import static co.coinfinity.AppConstants.PREF_KEY_PRODUCT_DETAIL_CONTRACT_ADDRESS_TESTNET;
import static co.coinfinity.AppConstants.TAG;

import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

public class BrandProtection_AddDetails extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IsoDep isoDep;
    private String pubKeyString;
    private String product_ID, product_Name,_time, _date, _manufacturer;
    Tag tag;
    private String ethAddress;
    private volatile boolean activityPaused = false;
    private InputErrorUtils inputErrorUtils;
    private String productNewAddress,productPublicKey;
    final Context context = this;
    private String manufacturerPublicKey, manufacturerEthAddress;

    @BindView(R.id.contractAddress)
    TextView contractAddress;
    @BindView(R.id.gasPrice)
    TextView gasPrice;
    @BindView(R.id.gasLimit)
    TextView gasLimit;
    @BindView(R.id.pin)
    TextView pinTxt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.productID)
    TextView productID;
    @BindView(R.id.productName)
    TextView productName;
    @BindView(R.id.timestamp)
    TextView time;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.manufacturer)
    TextView manufacturer;
    @BindView(R.id.submitBtn)
    Button submit;
    @BindView(R.id.keyIndexSpinner)
    Spinner keyIndexSpinner;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.textview)
    TextView textView;
    @BindView(R.id.productPublicKey)
    TextView _productAddress;
    @BindView(R.id.toggleButton)
    ToggleButton toggleButton;

    private void resolveIntent(Intent intent) {
        // Only handle NFC intents
        if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) == null) {
            return;
        }

        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        UiUtils.logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        if (toggleButton.isChecked()) {
            try {
                SharedPreferences pref = this.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
                productPublicKey = NfcUtils.readPublicKeyOrCreateIfNotExists(IsoTagWrapper.of(isoDep),
                        pref.getInt(KEY_INDEX_OF_CARD, 1)).getPublicKeyInHexWithoutPrefix();
                isoDep.close();
                if(productPublicKey.equals(manufacturerPublicKey)){
                    Toast.makeText(this,"tap the right product card...This is a manufacturer card", Toast.LENGTH_LONG).show();
                }else {
                    Log.d(TAG, String.format("pubkey read from card: '%s'", productPublicKey));
                    productNewAddress = Keys.toChecksumAddress(Keys.getAddress(productPublicKey));
                    showToast(String.format(getString(R.string.change_recipient_address), productNewAddress), this);
                    _productAddress.setText(productNewAddress);
                    toggleButton.toggle();
                }
            } catch (IOException | NfcCardException e) {
                showToast(e.getMessage(), this);
                Log.e(TAG, "Exception while reading public key from card: ", e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                SharedPreferences pref = this.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
                pubKeyString = NfcUtils.readPublicKeyOrCreateIfNotExists(IsoTagWrapper.of(isoDep),
                        pref.getInt(KEY_INDEX_OF_CARD, 1)).getPublicKeyInHexWithoutPrefix();
                isoDep.close();
            } catch (IOException | NfcCardException e) {
                showToast(e.getMessage(), this);
                Log.e(TAG, "Exception while reading public key from card: ", e);
                resetGuiState();
                return;
            }

            Log.d(TAG, String.format("pubkey read from card: '%s'", pubKeyString));
            // use web3j to format this public key as ETH address
            ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));

            if (!submit.isEnabled()) {
                textView.setVisibility(View.INVISIBLE);
                submit.setEnabled(true);
                productID.setVisibility(View.VISIBLE);
                productName.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
                manufacturer.setVisibility(View.VISIBLE);
                submit.setVisibility(View.VISIBLE);

                Toast.makeText(BrandProtection_AddDetails.this, "Hold the card for few seconds after clicking submit button",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * on clicking submit to uplaod product details into the smartcontract
     */
    public void onSubmit(View view) {

        String gasPriceValue=gasPrice.getText().toString();
        BigDecimal gasPriceDecimal=new BigDecimal(gasPriceValue);
        BigInteger gasPriceVal=gasPriceDecimal.toBigInteger();

        String gasLimitValue=gasLimit.getText().toString();
        BigDecimal gasLimitDecimal=new BigDecimal(gasLimitValue);
        BigInteger gasLimitVal=gasLimitDecimal.toBigInteger();
        product_ID = productID.getText().toString();
        product_Name = productName.getText().toString();
        _time = time.getText().toString();
        _date = date.getText().toString();
        _manufacturer = manufacturer.getText().toString();

        if(product_ID.equals("")){
            Toast.makeText(this,"Please enter product ID",Toast.LENGTH_LONG).show();
        }else if(product_Name.equals("")){
            Toast.makeText(this,"Please enter product Name",Toast.LENGTH_LONG).show();
        }else if(_time.equals("")){
            Toast.makeText(this,"Please enter time",Toast.LENGTH_LONG).show();
        }else if(_date.equals("")){
            Toast.makeText(this,"Please enter date",Toast.LENGTH_LONG).show();
        }else if(_manufacturer.equals("")){
            Toast.makeText(this,"Please enter manufacturer",Toast.LENGTH_LONG).show();
        }

            Thread thread = new Thread(() -> {
                try {
                    this.runOnUiThread(() -> Toast.makeText(BrandProtection_AddDetails.this, "Please wait... Hold the card for few seconds",
                            Toast.LENGTH_LONG).show());
                    ProductDetailUtils.productDetail(contractAddress.getText().toString(), productNewAddress, IsoDep.get(tag), pubKeyString, ethAddress, product_ID, product_Name, _time, _date, _manufacturer, gasPriceVal, gasLimitVal, productPublicKey, this);
                    this.runOnUiThread(() -> Toast.makeText(BrandProtection_AddDetails.this, "Product detail uploaded successfully",
                            Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    Log.e(TAG, "exception while uploading product detail: ", e);
                }
            });
            thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_protection__add_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            manufacturerPublicKey = bundle.getString("pubKey");
            System.out.println(manufacturerPublicKey);
            manufacturerEthAddress = bundle.getString("ethAddress");
        }

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(),"Dismissed..!!",Toast.LENGTH_SHORT).show();
        });
        dialog.show();

        inputErrorUtils = new InputErrorUtils(this, gasLimit, gasPrice,contractAddress);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            showToast(getString(R.string.no_nfc), this);
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        keyIndexSpinner.setSelection(1);
        SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        keyIndexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SharedPreferences.Editor mEditor = prefs.edit();
                mEditor.putInt(KEY_INDEX_OF_CARD, position)
                        .apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
       gasPrice.setText(prefs.getString(PREF_KEY_GASPRICE_WEI,GASPRICE));
       gasLimit.setText(prefs.getString(PREF_KEY_BRANDPROTECTION_GASLIMIT,GASLIMIT));
       contractAddress.setText(prefs.getString(PREF_KEY_PRODUCT_DETAIL_CONTRACT_ADDRESS_TESTNET,DEFAULT_PRODUCT_DETAIL_ADDRESS_TESTNET));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                openNfcSettings();
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        activityPaused = false;
    }

    /**
     * on scanQR code image button
      */
    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this, 0);
    }
    @Override
    protected void onPause() {
        activityPaused = true;
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    /**
     * Opens system settings, wireless settings.
     */
    private void openNfcSettings() {
        showToast(getString(R.string.enable_nfc), this);
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    /**
     * Called by Android systems whenever a new Intent is received. NFC tags are also
     * delivered via an Intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        activityPaused = false; // onPause() gets called when a Intent gets dispatched by Android
        setIntent(intent);
        resolveIntent(intent);
    }

    /**
     * If we have already a Public key, allow the user to reset by pressing back.
     */
    @Override
    public void onBackPressed() {
        if (pubKeyString != null) {
            resetGuiState();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * reset everything, like we never had seen a card.
     */
    private void resetGuiState() {

        pubKeyString = null;
        ethAddress = null;

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                contractAddress.setText(data.getStringExtra("SCAN_RESULT"));
            }
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
