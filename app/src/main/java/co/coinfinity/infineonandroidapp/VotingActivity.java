package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import co.coinfinity.infineonandroidapp.common.UiUtils;
import co.coinfinity.infineonandroidapp.ethereum.VotingUtils;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeScanner;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;
import java.util.List;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.TAG;

public class VotingActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

    private RadioGroup radioGroup;
    private EditText votingName;
    private EditText contractAddress;
    private EditText gasPrice;
    private EditText gasLimit;
    private TextView answer1Votes;
    private TextView answer2Votes;
    private TextView answer3Votes;
    private TextView answer4Votes;
    private TextView infoText;
    private ProgressBar progressBar;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        radioGroup = findViewById(R.id.radioGroup);
        votingName = findViewById(R.id.votingName);
        contractAddress = findViewById(R.id.contractAddress);
        gasPrice = findViewById(R.id.gasPrice);
        gasLimit = findViewById(R.id.gasLimit);
        answer1Votes = findViewById(R.id.answer1Votes);
        answer2Votes = findViewById(R.id.answer2Votes);
        answer3Votes = findViewById(R.id.answer3Votes);
        answer4Votes = findViewById(R.id.answer4Votes);
        infoText = findViewById(R.id.infoText);
        progressBar = findViewById(R.id.progressBar);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
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

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        String savedContractAddress = mPrefs.getString("contractAddress", "0x00aEBec0Feb36EF84454b41ee5214B3A46A43AA5");
        contractAddress.setText(savedContractAddress);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {
            handleAfterVote(mHandler);
        });
        thread.start();
    }

    @Override
    public void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Handler mHandler = new Handler();
        Thread thread = new Thread(() -> {

            final BigInteger gasLimit = getGasLimitFromString();
            final BigInteger gasPrice = getGasPriceFromString();

            final int votersAnswer = VotingUtils.getVotersAnswer(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
            if (votersAnswer == 0) {
                mHandler.post(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                });
                try {
                    this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, R.string.please_wait,
                            Toast.LENGTH_LONG).show());
                    VotingUtils.vote(contractAddress.getText().toString(), tagFromIntent, pubKeyString, ethAddress, votingName.getText().toString(), idx + 1, gasPrice, gasLimit);
                    this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, R.string.voted_successfully,
                            Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    Log.e(TAG, "exception while voting: ", e);
                    this.runOnUiThread(() -> Toast.makeText(VotingActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show());
                }
                handleAfterVote(mHandler);
            }
        });

        thread.start();

    }

    private void handleAfterVote(Handler mHandler) {
        final BigInteger gasLimit = getGasLimitFromString();
        final BigInteger gasPrice = getGasPriceFromString();

        final int votersAnswer = VotingUtils.getVotersAnswer(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
        if (votersAnswer != 0) {
            ((RadioButton) radioGroup.getChildAt(votersAnswer - 1)).setChecked(true);
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }

            final String votersName = VotingUtils.getVotersName(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
            votingName.setText(votersName);
            votingName.setEnabled(false);

            final List<Uint8> answerCounts = VotingUtils.getAnswerCounts(contractAddress.getText().toString(), ethAddress, gasPrice, gasLimit);
            mHandler.post(() -> {
                answer1Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(1).getValue().toString()));
                answer2Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(2).getValue().toString()));
                answer3Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(3).getValue().toString()));
                answer4Votes.setText(String.format(getString(R.string.votes_count), answerCounts.get(4).getValue().toString()));
                infoText.setText(R.string.already_voted);
            });
        }
        mHandler.post(() -> {
            progressBar.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("contractAddress", contractAddress.getText().toString()).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    public void scanQrCode(View view) {
        QrCodeScanner.scanQrCode(this);
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
