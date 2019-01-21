package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.coinfinity.infineonandroidapp.ethereum.CoinfinityClient;
import co.coinfinity.infineonandroidapp.ethereum.bean.EthBalanceBean;
import co.coinfinity.infineonandroidapp.ethereum.bean.TransactionPriceBean;
import co.coinfinity.infineonandroidapp.ethereum.utils.EthereumUtils;
import co.coinfinity.infineonandroidapp.infineon.NfcUtils;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import co.coinfinity.infineonandroidapp.qrcode.QrCodeGenerator;
import co.coinfinity.infineonandroidapp.utils.IsoTagWrapper;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import org.web3j.crypto.Keys;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static co.coinfinity.AppConstants.*;
import static co.coinfinity.infineonandroidapp.utils.UiUtils.showToast;

/**
 * Main activity. Entry point of the application.
 *
 * @author Coinfinity.co, 2018
 */
public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @BindView(R.id.ethAddress)
    TextView ethAddressView;
    @BindView(R.id.balance)
    TextView balance;
    @BindView(R.id.qrCode)
    ImageView qrCodeView;
    @BindView(R.id.holdCard)
    TextView holdCard;
    @BindView(R.id.send)
    Button sendEthBtn;
    @BindView(R.id.sendErc20)
    Button sendErc20Btn;
    @BindView(R.id.voting)
    Button votingBtn;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.image_nfc_icon)
    ImageView nfcIcon;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.keyIndexSpinner)
    Spinner keyIndexSpinner;

    private String pubKeyString;
    private String ethAddress;
    private EthBalanceBean ethBalance;

    private CoinfinityClient coinfinityClient = new CoinfinityClient();
    private volatile boolean activityPaused = false;

    /**
     * Will be called after card was hold to back of device.
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
        // now we have an IsoTag:

        // update UI
        displayOnUI(GuiState.PROGRESS_BAR);

        try {
            SharedPreferences pref = this.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            pubKeyString = NfcUtils.readPublicKeyOrCreateIfNotExists(IsoTagWrapper.of(isoDep),
                    pref.getInt(KEY_INDEX_OF_CARD, 1)).getPublicKeyInHexWithoutPrefix();
            isoDep.close();
        } catch (IOException | NfcCardException e) {
            showToast(e.getMessage(), this);
            Log.e(TAG, "Exception while reading public key from card: ", e);
            return;
        }
        Log.d(TAG, String.format("pubkey read from card: '%s'", pubKeyString));
        // use web3j to format this public key as ETH address
        ethAddress = Keys.toChecksumAddress(Keys.getAddress(pubKeyString));
        ethAddressView.setText(ethAddress);
        Log.d(TAG, String.format("ETH address: %s", ethAddress));
        qrCodeView.setImageBitmap(QrCodeGenerator.generateQrCode(ethAddress));
        holdCard.setText(R.string.card_found);
    }

    /**
     * this method is updating the balance and euro price on UI.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void updateBalance() throws Exception {
        Log.d(TAG, "reading ETH balance..");
        ethBalance = EthereumUtils.getBalance(ethAddress, UiUtils.getFullNodeUrl(this));
        Log.d(TAG, String.format("reading ETH balance finished: %s", balance.toString()));
    }

    /**
     * this method is updating the balance and euro price on UI.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void updateEuroPrice() throws Exception {
        if (ethBalance == null)
            return;

        Log.d(TAG, "reading EUR/ETH price..");
        TransactionPriceBean transactionPriceBean = coinfinityClient.readEuroPriceFromApiSync("0", "0",
                ethBalance.getEther().toString());
        Log.d(TAG, String.format("reading EUR/ETH price finished: %s", transactionPriceBean));
        if (transactionPriceBean != null && pubKeyString != null) {
            this.runOnUiThread(() -> {
                balance.setText(String.format("%s%s", ethBalance.toString(),
                        String.format(Locale.ENGLISH, "\nEuro: %.2f", transactionPriceBean.getPriceInEuro())));
                if (!sendEthBtn.isEnabled()) {
                    sendEthBtn.setEnabled(true);
                    sendErc20Btn.setEnabled(true);
                    votingBtn.setEnabled(true);
                }
                displayOnUI(GuiState.BALANCE_TEXT);
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        displayOnUI(GuiState.NFC_ICON);

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
                openWirelessSettings();
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        activityPaused = false;

        new Thread(() -> {
            Log.d(TAG, "Main activity, start reading eth balance thread...");
            try {
                while (!activityPaused && ethAddress != null) {
                    updateBalance();
                    TimeUnit.SECONDS.sleep(FIVE_SECONDS);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception while reading eth balance from api: ", e);
            }
            Log.d(TAG, "Main activity, reading eth balance thread exited.");
        }).start();

        new Thread(() -> {
            Log.d(TAG, "Main activity, start reading price thread...");
            try {
                if (ethAddress != null) {
                    updateBalance();
                    while (!activityPaused) {
                        updateEuroPrice();
                        TimeUnit.SECONDS.sleep(TEN_SECONDS);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "exception while reading euro price from api: ", e);
            }
            Log.d(TAG, "Main activity, reading price thread exited.");
        }).start();
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
    private void openWirelessSettings() {
        showToast(getString(R.string.enable_nfc), this);
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    /**
     * Called by Android systems whenever a new Intent is received. NFC tags are also
     * delivered via an Intent.
     *
     * @param intent
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
        displayOnUI(GuiState.NFC_ICON);
        pubKeyString = null;
        ethAddress = null;
        ethAddressView.setText("");
        qrCodeView.setImageBitmap(null);
        holdCard.setText(R.string.hold_card);
        sendEthBtn.setEnabled(false);
        sendErc20Btn.setEnabled(false);
        votingBtn.setEnabled(false);
    }

    private enum GuiState {NFC_ICON, PROGRESS_BAR, BALANCE_TEXT}

    /**
     * On button click SEND ETH.
     */
    public void onSend(View view) {
        Intent intent = new Intent(this, SendTransactionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * On button click SEND ERC-20.
     */
    public void onSendErc20(View view) {
        Intent intent = new Intent(this, SendErc20TokensActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * On button click VOTING.
     */
    public void onVoting(View view) {
//        Switch to new voting if needed
//        Intent intent = new Intent(this, VotingActivity.class);
        Intent intent = new Intent(this, VotingActivityOld.class);
        Bundle bundle = new Bundle();
        bundle.putString("pubKey", pubKeyString);
        bundle.putString("ethAddress", ethAddress);
        intent.putExtras(bundle);
        startActivity(intent);
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

    /**
     * Display only GUI elements of 1 of 3 states.
     * NFC Icon (when waiting for NFC), spinner (when waiting for network background tasks,
     * Text (when displaying balance results)
     *
     * @param state NFC_ICON, PROGRESS_BAR, BALANCE_TEXT
     */
    private void displayOnUI(GuiState state) {
        // only display NFC Icon
        if (GuiState.NFC_ICON.equals(state)) {
            progressBar.setVisibility(View.GONE);
            balance.setVisibility(View.GONE);
            nfcIcon.setVisibility(View.VISIBLE);
        }
        // only display progress bar
        else if (GuiState.PROGRESS_BAR.equals(state)) {
            nfcIcon.setVisibility(View.GONE);
            balance.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        // only display balance text
        else if (GuiState.BALANCE_TEXT.equals(state)) {
            nfcIcon.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            balance.setVisibility(View.VISIBLE);
        }
    }

}
