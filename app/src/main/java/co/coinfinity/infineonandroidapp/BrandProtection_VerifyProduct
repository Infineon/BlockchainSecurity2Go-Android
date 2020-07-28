package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Utf8;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.ChainId;
import org.web3j.utils.Bytes;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.utils.ProductDetailUtils;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GenerateSignatureResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeGenerator;
import co.coinfinity.infineonandroidapp.utils.ByteUtils;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;
import co.coinfinity.infineonandroidapp.utils.UiUtils;

import static co.coinfinity.AppConstants.DEFAULT_GASPRICE_IN_GIGAWEI;
import static co.coinfinity.AppConstants.DEFAULT_PRODUCT_DETAIL_ADDRESS_TESTNET;
import static co.coinfinity.AppConstants.GASLIMIT;
import static co.coinfinity.AppConstants.GASPRICE;
import static co.coinfinity.AppConstants.KEY_INDEX_OF_CARD;
import static co.coinfinity.AppConstants.PREFERENCE_FILENAME;
import static co.coinfinity.AppConstants.PREF_KEY_BRANDPROTECTION_GASLIMIT;
import static co.coinfinity.AppConstants.PREF_KEY_BRANDPROTECTION_GASPRICE;
import static co.coinfinity.AppConstants.PREF_KEY_GASPRICE_WEI;
import static co.coinfinity.AppConstants.PREF_KEY_MAIN_NETWORK;
import static co.coinfinity.AppConstants.PREF_KEY_PRODUCT_DETAIL_CONTRACT_ADDRESS_TESTNET;
import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;


public class BrandProtection_VerifyProduct extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String pubKeyString, pubKeyStringLower, ethAddress;
    private BigInteger pubKeyRecovered = null;

    @BindView(R.id.pId)
    TextView pId;
    @BindView(R.id.pName)
    TextView pName;
    @BindView(R.id.pTime)
    TextView pTime;
    @BindView(R.id.displayPid)
    TextView displayPid;
    @BindView(R.id.displayPname)
    TextView displayPname;
    @BindView(R.id.displaytime)
    TextView displaytime;
    @BindView(R.id.displaydate)
    TextView displaydate;
    @BindView(R.id.pDate)
    TextView pDate;
    @BindView(R.id.pManufacturer)
    TextView pManufacturer;
    @BindView(R.id.displayManufacturer)
    TextView displayManufacturer;
    @BindView(R.id.displayMessage)
    TextView displayMessage;
    @BindView(R.id.keyIndexSpinner)
    Spinner keyIndexSpinner;
    @BindView(R.id.verified)
    ImageView verified;
    @BindView(R.id.authSuccessful)
    TextView authSuccessful;
    private volatile boolean activityPaused = false;

    private void resolveIntent(Intent intent) {
        // Only handle NFC intents
        if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) == null) {
            return;
        }

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        UiUtils.logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }
        SharedPreferences pref = this.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        try {
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
        pubKeyStringLower=pubKeyString.toLowerCase();

        // signature generation and verification

        String data=getString(R.string.signatureData);
        String pin ="";
        byte[] pinByte=pin.getBytes();
        byte[] dataByte=data.getBytes();
        final byte[] hashedTransaction = Hash.sha3(dataByte);
        GenerateSignatureResponseApdu signedTransaction = null;
        try {
            signedTransaction = NfcUtils.generateSignature(IsoTagWrapper.of(isoDep), pref.getInt(KEY_INDEX_OF_CARD, 1), hashedTransaction, pinByte);
        } catch (IOException | NfcCardException e) {
            e.printStackTrace();
        }
        assert signedTransaction != null;
        Log.d(TAG, String.format("signed transaction: %s", ByteUtils.bytesToHex(signedTransaction.getSignature())));
        byte[] r = Bytes.trimLeadingZeroes(extractR(signedTransaction.getSignature()));
        byte[] s = Bytes.trimLeadingZeroes(extractS(signedTransaction.getSignature()));
        Log.d(TAG, String.format("r: %s", ByteUtils.bytesToHex(r)));
        Log.d(TAG, String.format("s: %s", ByteUtils.bytesToHex(s)));

        s = getCanonicalisedS(r, s);
        Log.d(TAG, String.format("s canonicalised: %s", ByteUtils.bytesToHex(s)));

        byte v = getV(pubKeyStringLower, hashedTransaction, r, s);
        Log.d(TAG, String.format("v: %s", v));

        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

        try {
            pubKeyRecovered = Sign.signedMessageToKey(data.getBytes(), signatureData);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        assert pubKeyRecovered != null;
        boolean validSig = pubKeyStringLower.equals(pubKeyRecovered.toString(16));
        if(validSig){
            displayMessage.setVisibility(View.INVISIBLE);
            verified.setVisibility(View.VISIBLE);
            authSuccessful.setVisibility(View.VISIBLE);
        }

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            String gasPriceValue=pref.getString(PREF_KEY_BRANDPROTECTION_GASPRICE,GASPRICE);
            BigDecimal gasPriceDecimal=new BigDecimal(gasPriceValue);
            BigInteger gasPriceVal=gasPriceDecimal.toBigInteger();

            String gasLimitValue= pref.getString(PREF_KEY_BRANDPROTECTION_GASLIMIT,GASLIMIT);
            BigDecimal gasLimitDecimal=new BigDecimal(gasLimitValue);
            BigInteger gasLimitVal=gasLimitDecimal.toBigInteger();


            try {
                this.runOnUiThread(() -> Toast.makeText(BrandProtection_VerifyProduct.this, "Please wait...",
                        Toast.LENGTH_LONG).show());
                final Tuple5<String, String, String, String, String> detail=ProductDetailUtils.getProductDetail(pref.getString(PREF_KEY_PRODUCT_DETAIL_CONTRACT_ADDRESS_TESTNET,DEFAULT_PRODUCT_DETAIL_ADDRESS_TESTNET), ethAddress, gasPriceVal, gasLimitVal, this);
                mHandler.post(()->{

                    verified.setVisibility(View.INVISIBLE);
                    displayMessage.setVisibility(View.INVISIBLE);
                    authSuccessful.setVisibility(View.INVISIBLE);
                    pId.setVisibility(View.VISIBLE);
                    displayPid.setVisibility(View.VISIBLE);
                    displayPname.setVisibility(View.VISIBLE);
                    pName.setVisibility(View.VISIBLE);
                    displaytime.setVisibility(View.VISIBLE);
                    pTime.setVisibility(View.VISIBLE);
                    displaydate.setVisibility(View.VISIBLE);
                    pDate.setVisibility(View.VISIBLE);
                    displayManufacturer.setVisibility(View.VISIBLE);
                    pManufacturer.setVisibility(View.VISIBLE);

                    pId.setText(detail.getValue1());
                    pName.setText(detail.getValue2());
                    pTime.setText(detail.getValue3());
                    pDate.setText(detail.getValue4());
                    pManufacturer.setText(detail.getValue5());
                });
                this.runOnUiThread(() -> Toast.makeText(BrandProtection_VerifyProduct.this, "product details read successfully",
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "exception while reading product details: ", e);
            }

        });

        thread.start();

    }
    private static byte[] getCanonicalisedS(byte[] r, byte[] s) {
        ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        ecdsaSignature = ecdsaSignature.toCanonicalised();
        return ecdsaSignature.s.toByteArray();
    }

    private static byte[] extractR(byte[] signature) {
        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
        int lengthR = signature[startR + 1];
        return Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR);
    }

    private static byte[] extractS(byte[] signature) {
        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
        int lengthR = signature[startR + 1];
        int startS = startR + 2 + lengthR;
        int lengthS = signature[startS + 1];
        return Arrays.copyOfRange(signature, startS + 2, startS + 2 + lengthS);
    }

    private static byte getV(String publicKey, byte[] hashedTransaction, byte[] r, byte[] s) {
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {

            //calls private method form web3j lib
            BigInteger k = Sign.recoverFromSignature(i, sig, hashedTransaction);

            if (k != null && k.equals(new BigInteger(1, ByteUtils.fromHexString(publicKey)))) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException(
                    "Could not construct a recoverable key. This should never happen.");
        }

        int headerByte = recId + 27;
        // 1 header + 32 bytes for R + 32 bytes for S
        return (byte) headerByte;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_protection__verify_product);

        ButterKnife.bind(this);
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
        keyIndexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SharedPreferences prefs = parentView.getContext().getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor mEditor = prefs.edit();
                mEditor.putInt(KEY_INDEX_OF_CARD, position)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
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


}
