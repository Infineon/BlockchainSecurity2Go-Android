package co.coinfinity.infineonandroidapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;
import co.coinfinity.infineonandroidapp.utils.UiUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static android.app.PendingIntent.getActivity;
import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Activity class used for changing PIN functionality.
 */
public class ChangePinActivity extends AppCompatActivity {

    @BindView(R.id.oldPin)
    TextView oldPin;
    @BindView(R.id.newPin)
    TextView newPin;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
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
        UiUtils.logTagInfo(tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            showToast(getString(R.string.wrong_card), this);
            return;
        }

        try {
            final String puk = bytesToHex(NfcUtils.changePin(IsoTagWrapper.of(isoDep),
                    oldPin.getText().toString().getBytes(StandardCharsets.UTF_8),
                    newPin.getText().toString().getBytes(StandardCharsets.UTF_8)));

            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle(R.string.chang_pin)
                    .setMessage(String.format(getString(R.string.change_pin_message),
                            oldPin.getText(), newPin.getText(), puk))
                    .setPositiveButton(R.string.copy_puk_to_clipboard, ((dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(getString(R.string.puk_code), puk);
                        clipboard.setPrimaryClip(clip);
                        UiUtils.showToast(getString(R.string.puk_copied), ChangePinActivity.this);
                    }));
                    //.setPositiveButton(R.string.ok, (dialog, which) -> finish());
            alert.show();
        } catch (IOException | NfcCardException e) {
            showToast(e.getMessage(), this);
            Log.e(TAG, "Exception while changing PIN", e);
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
