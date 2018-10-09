package co.coinfinity.infineonandroidapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import co.coinfinity.infineonandroidapp.R;

/**
 * Utility for Activities.
 */
public class UiUtils {

    /**
     * Handle option menu click
     *
     * @param act  activity
     * @param item selected menuitem
     * @return true if handled in here, false otherwise
     */
    public static boolean handleOptionItemSelected(Activity act, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.visit_website:
                String url = "http://www.coinfinity.co";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                act.startActivity(i);
                return true;
            default:
                return false;

        }
    }
}
