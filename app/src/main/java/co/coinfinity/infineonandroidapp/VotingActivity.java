package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
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
import co.coinfinity.infineonandroidapp.utils.InputErrorUtils;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import co.coinfinity.infineonandroidapp.ethereum.VotingUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint32;

import java.math.BigInteger;
import java.util.List;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;

public class VotingActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

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
    @BindView(R.id.infoText)
    TextView infoText;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private InputErrorUtils inputErrorUtils;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        inputErrorUtils = new InputErrorUtils(this, gasPrice, gasLimit, contractAddress);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
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
            Handler handler = new Handler();
            new Thread(() -> updateVoteState(handler)).start();
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
        // TODO check IsoDep
        IsoDep isoDep = IsoDep.get(tagFromIntent);


        Handler handler = new Handler();
        new Thread(() -> {

            final BigInteger gasLimit = getGasLimitFromString();
            final BigInteger gasPrice = getGasPriceFromString();

            try {
                handler.post(() -> progressBar.setVisibility(View.VISIBLE));

                this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, R.string.please_wait,
                        Toast.LENGTH_SHORT).show());
                VotingUtils.vote(contractAddress.getText().toString(), isoDep, pubKeyString, ethAddress, votingName.getText().toString(), idx, gasPrice, gasLimit);
                this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, R.string.voted_successfully,
                        Toast.LENGTH_SHORT).show());

                updateVoteState(handler);
            } catch (Exception e) {
                if (e.getCause() != null && "Empty value (0x) returned from contract".contains(e.getCause().getMessage())) {
                    this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, "Wrong contract address!",
                            Toast.LENGTH_SHORT).show());
                }
                Log.e(TAG, "exception while voting: ", e);
                this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, "Could not vote!", Toast.LENGTH_SHORT).show());
            }

        }).start();

    }

    private void updateVoteState(Handler handler) {
        try {
            final BigInteger gasLimit = getGasLimitFromString();
            final BigInteger gasPrice = getGasPriceFromString();

            final Bool voterExists = VotingUtils.voterExists(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
            if (voterExists.getValue()) {
                final int votersAnswer = VotingUtils.getVotersAnswer(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit).intValue();
                final String votersName = VotingUtils.getVotersName(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
                final List<Uint32> answerCounts = VotingUtils.getCurrentResult(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);

                handler.post(() -> {
                    // this has to be done in the the UI thread
                    ((RadioButton) radioGroup.getChildAt(votersAnswer)).setChecked(true);
                    for (int i = 0; i < radioGroup.getChildCount(); i++) {
                        radioGroup.getChildAt(i).setEnabled(false);
                    }

                    votingName.setText(votersName);
                    votingName.setEnabled(false);

                    if (answerCounts != null) {
                        answer1Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(0).getValue().toString()));
                        answer2Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(1).getValue().toString()));
                        answer3Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(2).getValue().toString()));
                        answer4Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(3).getValue().toString()));
                    }
                    infoText.setText(R.string.already_voted);
                });

                handler.post(() -> progressBar.setVisibility(View.INVISIBLE));
            }
        } catch (Exception e) {
            if (e.getCause() != null && "Empty value (0x) returned from contract".contains(e.getCause().getMessage())) {
                this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, "Wrong contract address!",
                        Toast.LENGTH_SHORT).show());
            }
            Log.e(TAG, "exception after vote handling", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);

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
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
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

    private BigInteger getGasPriceFromString() {
        String gasPriceStr = gasPrice.getText().toString();
        BigInteger gasPrice;
        if (gasPriceStr.equals("")) {
            gasPrice = new BigInteger("0");
        } else {
            gasPrice = new BigInteger(gasPriceStr);
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
        return UiUtils.handleOptionITemSelected(this, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
