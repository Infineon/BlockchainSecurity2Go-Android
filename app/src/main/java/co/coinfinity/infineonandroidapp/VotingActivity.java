package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import co.coinfinity.infineonandroidapp.ethereum.VotingUtils;
import org.web3j.abi.datatypes.generated.Uint8;

import java.util.List;

import static android.app.PendingIntent.getActivity;

public class VotingActivity extends AppCompatActivity {

    private String pubKeyString;
    private String ethAddress;

    private RadioGroup radioGroup;
    private EditText votingName;
    private EditText contractAddress;
    private TextView answer1Votes;
    private TextView answer2Votes;
    private TextView answer3Votes;
    private TextView answer4Votes;

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
        answer1Votes = findViewById(R.id.answer1Votes);
        answer2Votes = findViewById(R.id.answer2Votes);
        answer3Votes = findViewById(R.id.answer3Votes);
        answer4Votes = findViewById(R.id.answer4Votes);

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

            final int votersAnswer = VotingUtils.getVotersAnswer(contractAddress.getText().toString(), ethAddress);
            if (votersAnswer == 0) {
                VotingUtils.vote(contractAddress.getText().toString(), tagFromIntent, pubKeyString, ethAddress, votingName.getText().toString(), idx + 1);
                handleAfterVote(mHandler);
            }
        });

        thread.start();

    }

    private void handleAfterVote(Handler mHandler) {
        final int votersAnswer = VotingUtils.getVotersAnswer(contractAddress.getText().toString(), ethAddress);
        ((RadioButton) radioGroup.getChildAt(votersAnswer - 1)).setChecked(true);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }

        final String votersName = VotingUtils.getVotersName(contractAddress.getText().toString(), ethAddress);
        votingName.setText(votersName);
        votingName.setEnabled(false);

        final List<Uint8> answerCounts = VotingUtils.getAnswerCounts(contractAddress.getText().toString(), ethAddress);
        mHandler.post(() -> {
            answer1Votes.setText(answerCounts.get(1).getValue().toString());
            answer2Votes.setText(answerCounts.get(2).getValue().toString());
            answer3Votes.setText(answerCounts.get(3).getValue().toString());
            answer4Votes.setText(answerCounts.get(4).getValue().toString());
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }
}
