package co.coinfinity.infineonandroidapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.MainActivity;
import co.coinfinity.infineonandroidapp.R;
import co.coinfinity.infineonandroidapp.SendErc20TokensActivity;
import co.coinfinity.infineonandroidapp.SendTransactionActivity;

import static co.coinfinity.AppConstants.*;

/**
 * Utility for Activities.
 */
public class UiUtils {

    /**
     * Handle option menu click
     *
     * @param activity  activity
     * @param item selected menuitem
     * @return true if handled in here, false otherwise
     */
    public static boolean handleOptionItemSelected(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.visit_website:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(COINFINITY_BASE_URL));
                activity.startActivity(i);
                return true;
            case R.id.refresh_balance:
                new Thread(() -> {
                    try {
                        Log.d(TAG, "Manual refresh..");
                        refreshBalance(activity);
                        Log.d(TAG, "Manual refresh finished.");
                    } catch (Exception e) {
                        showToast(activity.getString(R.string.could_not_refresh), activity);
                        Log.e(TAG, "Error on manual refresh", e);
                    }
                }).start();
                return true;
            case R.id.switch_network:
                SharedPreferences prefs = activity.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
                boolean isMainNetwork = prefs.getBoolean(PREF_KEY_MAIN_NETWORK, true);

                String strNetwork = "main network";
                if (isMainNetwork) strNetwork = "test network";
                String finalStrNetwork = strNetwork;
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.switch_network)
                        .setMessage(String.format(activity.getString(R.string.ask_switch_network), strNetwork))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            SharedPreferences.Editor mEditor = prefs.edit();
                            mEditor.putBoolean(PREF_KEY_MAIN_NETWORK, !isMainNetwork).apply();
                            showToast(String.format(activity.getString(R.string.switched_to), finalStrNetwork), activity);
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            default:
                return false;

        }
    }

    private static void refreshBalance(Activity act) throws Exception {
        if (act instanceof MainActivity) {
            ((MainActivity) act).updateBalance();
            ((MainActivity) act).updateEuroPrice();
        } else if (act instanceof SendTransactionActivity) {
            ((SendTransactionActivity) act).updateReadingEuroPrice();
        } else if (act instanceof SendErc20TokensActivity) {
            ((SendErc20TokensActivity) act).readAndDisplayErc20Balance();
        }
    }

    /**
     * Method used to log NFC tag info.
     *
     * @param tagFromIntent actual tag to use
     */
    public static void logTagInfo(Tag tagFromIntent) {
        Log.d(TAG, String.format("NFC Tag detected: %s", tagFromIntent.toString()));
        Log.d(TAG, String.format("NFC Tag id: %s", ByteUtils.bytesToHex(tagFromIntent.getId())));
    }

    /**
     * Method to get url of the network to connect (Mainnet or Testnet)
     *
     * @param activity read shared prefs from
     * @return mainnet or testnet url
     */
    public static String getFullNodeUrl(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_KEY_MAIN_NETWORK, true))
            return MAINNET_URI;

        return ROPSTEN_URI;
    }

    /**
     * Method used to show toast message on UI thread
     *
     * @param text     message to show
     * @param activity needed for the context
     */
    public static void showToast(String text, Activity activity) {
        activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }
}
