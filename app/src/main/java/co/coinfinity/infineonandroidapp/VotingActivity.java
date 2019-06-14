package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.exceptions.InvalidEthereumAddressException;
import co.coinfinity.infineonandroidapp.ethereum.utils.UriUtils;
import co.coinfinity.infineonandroidapp.ethereum.utils.VotingUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.InvalidContractException;
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
    @BindView(R.id.answer1)
    TextView answer1;
    @BindView(R.id.answer2)
    TextView answer2;
    @BindView(R.id.answer3)
    TextView answer3;
    @BindView(R.id.answer4)
    TextView answer4;
    @BindView(R.id.pin)
    TextView pinTxt;

    private InputErrorUtils inputErrorUtils;

    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;

    private UnitSpinnerAdapter spinnerAdapter = new UnitSpinnerAdapter();

    private String[] votingAnswers;

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

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);

        String savedContractAddress = pref.getString(PREF_KEY_VOTING_CONTRACT_ADDRESS, DEFAULT_VOTING_CONTRACT_ADDRESS);
        if (!pref.getBoolean(PREF_KEY_MAIN_NETWORK, true)) {
            savedContractAddress = pref.getString(PREF_KEY_VOTING_CONTRACT_ADDRESS_TESTNET, DEFAULT_VOTING_CONTRACT_ADDRESS_TESTNET);
        }

        gasLimit.setText(pref.getString(PREF_KEY_VOTING_GASLIMIT, DEFAULT_GASLIMIT));
        gasPrice.setText(pref.getString(PREF_KEY_GASPRICE_WEI, DEFAULT_GASPRICE_IN_GIGAWEI));
        pinTxt.setText(pref.getString(PREF_KEY_PIN, ""));

        //if (!savedContractAddress.isEmpty()) {
            contractAddress.setText(savedContractAddress);
        //}

        votingAnswers = new String[]{getString(R.string.answer_1), getString(R.string.answer_2), getString(R.string.answer_3), getString(R.string.answer_4)};
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
                StaticArray4<Address> whiteListResponse = VotingUtils.whitelistedSenderAddresses(
                        contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit, UiUtils.getFullNodeUrl(this));
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
                    showToast(String.format(getString(R.string.voting_please_wait), votingAnswers[indexOfAnswer]), this);

                    this.runOnUiThread(() -> {
                        TextView viewOfVote = null;
                        switch (indexOfAnswer) {
                            case 0:
                                viewOfVote = answer1;
                                break;
                            case 1:
                                viewOfVote = answer2;
                                break;
                            case 2:
                                viewOfVote = answer3;
                                break;
                            case 3:
                                viewOfVote = answer4;
                                break;
                        }
                        UiUtils.startBlinkingAnimation(viewOfVote, 500, 5000);
                    });

                    Log.d(TAG, "sending vote.. ");
                    final TransactionReceipt response = VotingUtils.vote(contractAddress.getText().toString(), isoDep,
                            pubKeyString, ethAddress, gasPrice, gasLimit, this, UiUtils.getFullNodeUrl(this));
                    Log.d(TAG, String.format("sending vote finished with Hash: %s", response.getTransactionHash()));
                } else {
                    showToast(getString(R.string.card_not_whitelisted), this);
                }
            } catch (InvalidContractException ice) {
                Log.e(TAG, "exception while voting: ", ice);
                showToast(getString(R.string.incorrect_contract), this);
            } catch (Exception e) {
                Log.e(TAG, "exception while voting: ", e);
                showToast(getString(R.string.could_not_vote), this);
            }
        }).start();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

        SharedPreferences mPrefs = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_KEY_VOTING_GASLIMIT, gasLimit.getText().toString())
                .putString(PREF_KEY_GASPRICE_WEI, gasPrice.getText().toString())
                .putString(PREF_KEY_PIN, pinTxt.getText().toString());

        if (mPrefs.getBoolean(PREF_KEY_MAIN_NETWORK, true)) {
            mEditor.putString(PREF_KEY_VOTING_CONTRACT_ADDRESS, contractAddress.getText().toString());
        } else {
            mEditor.putString(PREF_KEY_VOTING_CONTRACT_ADDRESS_TESTNET, contractAddress.getText().toString());
        }

        mEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == 0) {
                    contractAddress.setText(UriUtils.extractEtherAddressFromUri(data.getStringExtra("SCAN_RESULT")));
                }
            } catch (InvalidEthereumAddressException e) {
                Log.e(TAG, "Exception on reading ethereum address", e);
                showToast(getString(R.string.invalid_ethereum_address), this);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "QR Code scanning canceled.");
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
