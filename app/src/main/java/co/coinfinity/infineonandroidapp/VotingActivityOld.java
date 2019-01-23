package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.adapter.UnitSpinnerAdapter;
import co.coinfinity.infineonandroidapp.ethereum.utils.VotingUtilsOld;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import org.web3j.abi.datatypes.generated.Uint32;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

public class VotingActivityOld extends AppCompatActivity {

    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.votingName)
    EditText votingName;
    @BindView(R.id.contractAddress)
    EditText contractAddress;
    @BindView(R.id.gasPrice)
    EditText gasPrice;
    @BindView(R.id.gasLimit)
    EditText gasLimit;
    @BindView(R.id.answer1Votes)
    TextView answer1Votes;
    @BindView(R.id.answer2Votes)
    TextView answer2Votes;
    @BindView(R.id.answer3Votes)
    TextView answer3Votes;
    @BindView(R.id.answer4Votes)
    TextView answer4Votes;
    @BindView(R.id.pin)
    TextView pinTxt;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String pubKeyString;
    private String ethAddress;
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;

    private InputErrorUtils inputErrorUtils;

    private UnitSpinnerAdapter spinnerAdapter = new UnitSpinnerAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_old);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinnerAdapter.addSpinnerAdapter(this, spinner);
        inputErrorUtils = new InputErrorUtils(gasPrice, gasLimit, contractAddress, votingName, this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pubKeyString = b.getString("pubKey");
            ethAddress = b.getString("ethAddress");
        }

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);

        gasLimit.setText(pref.getString(PREF_KEY_VOTING_GASLIMIT, DEFAULT_GASLIMIT));
        gasPrice.setText(pref.getString(PREF_KEY_GASPRICE_WEI, DEFAULT_GASPRICE_IN_GIGAWEI));
        pinTxt.setText(pref.getString(PREF_KEY_PIN, ""));

        reloadVotes(pref);
    }

    private void reloadVotes(SharedPreferences pref) {
        String savedContractAddress = pref.getString(PREF_KEY_VOTING_CONTRACT_ADDRESS, DEFAULT_VOTING_CONTRACT_ADDRESS);
        if (!pref.getBoolean(PREF_KEY_MAIN_NETWORK, true)) {
            savedContractAddress = pref.getString(PREF_KEY_VOTING_CONTRACT_ADDRESS_TESTNET, DEFAULT_VOTING_CONTRACT_ADDRESS_TESTNET);
        }
        if (!savedContractAddress.isEmpty()) {
            contractAddress.setText(savedContractAddress);
            Handler mHandler = new Handler();
            new Thread(() -> {
                try {
                    handleAfterVote(mHandler);
                } catch (Exception e) {
                    Log.e(TAG, "exception handle after vote: ", e);
                    showToast(getString(R.string.problem_reload_votes), this);
                }
            }).start();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (inputErrorUtils.isNoInputError()) {
            resolveIntent(intent);
        }
    }

    private void resolveIntent(Intent intent) {
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {

            final BigInteger gasLimit = getGasLimitFromString();
            BigInteger gasPrice = getGasPriceFromString().multiply(spinnerAdapter.getMultiplier()).toBigInteger();

            mHandler.post(() -> {
                progressBar.setVisibility(View.VISIBLE);
            });
            try {
                this.runOnUiThread(() -> Toast.makeText(VotingActivityOld.this, "Please wait...",
                        Toast.LENGTH_LONG).show());
                VotingUtilsOld.vote(contractAddress.getText().toString(), IsoDep.get(tagFromIntent), pubKeyString, ethAddress, votingName.getText().toString(), idx, gasPrice, gasLimit, this);
                this.runOnUiThread(() -> Toast.makeText(VotingActivityOld.this, "Voted successfully",
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "exception while voting: ", e);
                this.runOnUiThread(() -> Toast.makeText(VotingActivityOld.this, e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
            try {
                handleAfterVote(mHandler);
            } catch (Exception e) {
                Log.e(TAG, "exception handle after vote: ", e);
                showToast(getString(R.string.problem_after_vote), this);
            }

        });

        thread.start();

    }

    private void handleAfterVote(Handler mHandler) throws Exception {
        final BigInteger gasLimit = getGasLimitFromString();
        BigInteger gasPrice = getGasPriceFromString().multiply(spinnerAdapter.getMultiplier()).toBigInteger();

        final List<Uint32> answerCounts = VotingUtilsOld.getCurrentResult(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit, this);
        mHandler.post(() -> {
            answer1Votes.setText(String.format("Votes: %s", answerCounts.get(0).getValue().toString()));
            answer2Votes.setText(String.format("Votes: %s", answerCounts.get(1).getValue().toString()));
            answer3Votes.setText(String.format("Votes: %s", answerCounts.get(2).getValue().toString()));
            answer4Votes.setText(String.format("Votes: %s", answerCounts.get(3).getValue().toString()));
        });
        mHandler.post(() -> progressBar.setVisibility(View.INVISIBLE));
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

        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        reloadVotes(pref);
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
            }
            if (resultCode == RESULT_CANCELED) {
                //handle cancel
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
