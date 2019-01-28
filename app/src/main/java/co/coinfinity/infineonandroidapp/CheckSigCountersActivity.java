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
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GetKeyInfoResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;

import java.io.IOException;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.logTagInfo;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Activity class used for checking signature counters functionality.
 */
public class CheckSigCountersActivity extends AppCompatActivity {

    @BindView(R.id.sigCount)
    TextView sigCount;
    @BindView(R.id.globalSigCount)
    TextView globalSigCount;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sig_counters);
        ButterKnife.bind(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        pendingIntent = getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    /**
     * Called by Android systems whenever a new Intent is received. NFC tags are also
     * delivered via an Intent.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    /* Will be called after card was hold to back of device.
     *
     * @param intent includes nfc extras
     */
    private void resolveIntent(Intent intent) {
        // Only handle NFC intents
        if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) == null) {
            return;
        }

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        try {
            SharedPreferences pref = this.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            int keyIndex = pref.getInt(KEY_INDEX_OF_CARD, 1);
            final GetKeyInfoResponseApdu responseApdu = NfcUtils.readPublicKeyOrCreateIfNotExists(
                    IsoTagWrapper.of(isoDep), keyIndex);
            globalSigCount.setText(String.format(getString(R.string.global_sig_counter),
                    responseApdu.getGlobalSigCounterAsInteger()));
            sigCount.setText(String.format(getString(R.string.sig_counter), keyIndex,
                    responseApdu.getSigCounterAsInteger()));
        } catch (IOException | NfcCardException e) {
            showToast(e.getMessage(), this);
            Log.e(TAG, "Exception while generating key from seed", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }
}
