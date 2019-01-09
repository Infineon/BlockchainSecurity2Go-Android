package co.coinfinity.infineonandroidapp.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.*;

import static co.coinfinity.AppConstants.*;

/**
 * Utility for Activities.
 */
public class UiUtils {

    /**
     * This method will start a blinking animation for given TextView.
     *
     * @param view TextView to start animation on
     */
    public static void startBlinkingAnimation(TextView view, int blinkingDuration, int totalDuration) {
        if (view != null) {
            final ObjectAnimator animator = ObjectAnimator.ofInt(view, "textColor", Color.BLACK, Color.TRANSPARENT);
            animator.setDuration(blinkingDuration);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();

            Handler handler = new Handler();
            handler.postDelayed(animator::cancel, totalDuration);
        }
    }

    /**
     * Handle option menu click.
     *
     * @param activity activity
     * @param item     selected menuitem
     * @return true if handled in here, false otherwise
     */
    public static boolean handleOptionItemSelected(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent intent = new Intent(activity, AboutActivity.class);
                activity.startActivity(intent);
                return true;
            case R.id.refresh_balance:
                showToast(activity.getString(R.string.refreshing), activity);
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
                if (isMainNetwork) {
                    strNetwork = "test network";
                }
                String finalStrNetwork = strNetwork;
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.switch_network)
                        .setMessage(String.format(activity.getString(R.string.ask_switch_network), strNetwork))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            SharedPreferences.Editor mEditor = prefs.edit();
                            mEditor.putBoolean(PREF_KEY_MAIN_NETWORK, !isMainNetwork).apply();
                            showToast(String.format(activity.getString(R.string.switched_to), finalStrNetwork), activity);
//                            activity.recreate();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            case R.id.set_pin:
                Intent setPinIntent = new Intent(activity, SetPinActivity.class);
                activity.startActivity(setPinIntent);
                return true;
            case R.id.change_pin:
                Intent changePinIntent = new Intent(activity, ChangePinActivity.class);
                activity.startActivity(changePinIntent);
                return true;
            case R.id.unlock_pin:
                Intent unlockPinIntent = new Intent(activity, UnlockPinActivity.class);
                activity.startActivity(unlockPinIntent);
                return true;
            case R.id.generate_from_seed:
                Intent generateFromSeedIntent = new Intent(activity, GenerateFromSeedActivity.class);
                activity.startActivity(generateFromSeedIntent);
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
     * Method to get url of the network to connect (Mainnet or Testnet).
     *
     * @param activity read shared prefs from
     * @return mainnet or testnet url
     */
    public static String getFullNodeUrl(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_KEY_MAIN_NETWORK, true)) {
            return MAINNET_URI;
        }

        return ROPSTEN_URI;
    }

    /**
     * Method used to show toast message on UI thread.
     *
     * @param text     message to show
     * @param activity needed for the context
     */
    public static void showToast(String text, Activity activity) {
        activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }
}
