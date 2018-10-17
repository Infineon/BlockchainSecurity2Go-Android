package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.utils.VotingUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.logTagInfo;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Activity class used for voting via smart contracts.
 */
public class VotingActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

    @BindView(R.id.contractAddress)
    EditText contractAddress;
    @BindView(R.id.gasPrice)
    EditText gasPrice;
    @BindView(R.id.gasLimit)
    EditText gasLimit;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.spinner)
    Spinner spinner;

    private InputErrorUtils inputErrorUtils;

    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;

    private UnitSpinnerAdapter spinnerAdapter = new UnitSpinnerAdapter();

    private String[] votingAnswer = {"1 billion", "2 billion", "3 billion", "4 billion"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinnerAdapter.addSpinnerAdapter(this, spinner);
        inputErrorUtils = new InputErrorUtils(this, gasPrice, gasLimit, contractAddress);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pubKeyString = bundle.getString("pubKey");
            ethAddress = bundle.getString("ethAddress");
        }

        SharedPreferences pref = getSharedPreferences("label", 0);
        String savedContractAddress = pref.getString(PREF_KEY_VOTING_CONTRACT_ADDRESS, "");
        gasLimit.setText(pref.getString(PREF_KEY_VOTING_GASLIMIT, "100000"));
        gasPrice.setText(pref.getString(PREF_KEY_GASPRICE_WEI, "21"));

        if (!savedContractAddress.isEmpty()) {
            contractAddress.setText(savedContractAddress);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (inputErrorUtils.isNoInputError()) {
            resolveIntent(intent);
        }
    }

    /**
     * will be called after card was hold to back of device
     *
     * @param intent includes nfc extras
     */
    private void resolveIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        new Thread(() -> {
            final BigInteger gasLimit = getGasLimitFromString();
            BigInteger gasPrice = getGasPriceFromString().multiply(spinnerAdapter.getMultiplier()).toBigInteger();

            try {
                Log.d(TAG, "checking if address is whitelisted.. ");
                StaticArray4<Address> whiteListResponse = VotingUtils.whitelistedSenderAddresses(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
                Log.d(TAG, "finished checking if whitelisted.");
                final List<String> addresses = whiteListResponse.getValue()
                        .stream()
                        .map(address -> address.getValue().toUpperCase())
                        .collect(Collectors.toList());

                final boolean whiteListed = addresses
                        .stream()
                        .anyMatch(address -> address.equals(ethAddress.toUpperCase()));

                if (whiteListed) {
                    final int indexOfAnswer = addresses.indexOf(ethAddress.toUpperCase());
                    showToast(String.format("Voting for %s. Please wait...", votingAnswer[indexOfAnswer]), this);

                    Log.d(TAG, "sending vote.. ");
                    final TransactionReceipt response = VotingUtils.vote(contractAddress.getText().toString(), isoDep, pubKeyString, ethAddress, gasPrice, gasLimit, this);
                    Log.d(TAG, String.format("sending vote finished with Hash: %s", response.getTransactionHash()));
                } else {
                    showToast("This card is not whitelisted!", this);
                }
            } catch (Exception e) {
                if (e.getCause() != null && "Empty value (0x) returned from contract".contains(e.getCause().getMessage())) {
                    showToast("Wrong contract address!", this);
                }
                Log.e(TAG, "exception while voting: ", e);
                showToast("Could not vote!", this);
            }
        }).start();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor
                .putString(PREF_KEY_VOTING_CONTRACT_ADDRESS, contractAddress.getText().toString())
                .putString(PREF_KEY_VOTING_GASLIMIT, gasLimit.getText().toString())
                .putString(PREF_KEY_GASPRICE_WEI, gasPrice.getText().toString())
                .apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contractAddress.setText(data.getStringExtra("SCAN_RESULT"));
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "QR Code scanning canceled.");
            }
        }
    }

    private BigDecimal getGasPriceFromString() {
        String gasPriceStr = gasPrice.getText().toString();
        BigDecimal gasPrice;
        if (gasPriceStr.equals("")) {
            gasPrice = new BigDecimal("0");
        } else {
            gasPrice = new BigDecimal(gasPriceStr);
        }

        return gasPrice;
    }

    private BigInteger getGasLimitFromString() {
        String gasLimitStr = gasLimit.getText().toString();
        BigInteger gasLimit;
        if (gasLimitStr.equals("")) {
            gasLimit = new BigInteger("0");
        } else {
            gasLimit = new BigInteger(gasLimitStr);
        }

        return gasLimit;
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
