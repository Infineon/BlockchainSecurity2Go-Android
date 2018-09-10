package co.coinfinity.infineonandroidapp.common;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import co.coinfinity.infineonandroidapp.R;

public class UiUtils {
    public static boolean handleOptionITemSelected(AppCompatActivity appCompatActivity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.visit_website:
                String url = "http://www.coinfinity.co";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                appCompatActivity.startActivity(i);
                return true;
            default:
                return false;

        }
    }
}
