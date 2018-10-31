package co.coinfinity.infineonandroidapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.utils.UiUtils;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

import static co.coinfinity.AppConstants.TAG;

public class AboutActivity extends AppCompatActivity {

    private int numTimesVersionClicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            View aboutPage = new AboutPage(this)
                    .isRTL(false)
                    .setDescription("This App was developed by Coinfinity GmbH to demonstrate the Infineon NFC card functionality via Ethereum blockchain.")
                    .addGroup("Connect with Coinfinity GmbH")
                    .addEmail("office@coinfinity.co")
                    .addWebsite("http://coinfinity.co/")
                    .addFacebook("coinfinitygmbh")
                    .addTwitter("coinfinityco")
                    .addYoutube("UCjgUYpQD00RB8eozRrqLhBg")
                    .addGroup("Connect with Infineon Technologies AG")
                    .addWebsite("http://www.infineon.com/")
                    .addFacebook("Infineon")
                    .addTwitter("Infineon")
                    .addGroup("This app uses")
                    .addGitHub("web3j/web3j", "Web3J - Ethereum")
                    .addGitHub("zxing/zxing", "ZXing - QR code")
                    .addGitHub("JakeWharton/butterknife", "Butterknife")
                    .addGitHub("medyo/android-about-page", "Android About Page")
                    .addWebsite("http://thenounproject.com/icon/74440/", "QR code icon by Rohith M S")
                    .addItem(getVersionElement())
                    .create();


            setContentView(aboutPage);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Exception while getting about page info: ", e);
        }
    }

    private Element getVersionElement() throws PackageManager.NameNotFoundException {
        Element apkVersion = new Element();
        apkVersion.setTitle("Apk Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        apkVersion.setOnClickListener(v -> {
            switch (++numTimesVersionClicked) {
                case 4:
                    UiUtils.showToast("3", this);
                    break;
                case 5:
                    UiUtils.showToast("2", this);
                    break;
                case 6:
                    UiUtils.showToast("1", this);
                    break;
                case 7:
                    numTimesVersionClicked = 0;
                    UiUtils.showToast("Are you Satoshi?", this);
                    Toast toast = new Toast(this);
                    ImageView view = new ImageView(this);
                    view.setImageResource(R.drawable.nope);
                    toast.setView(view);
                    toast.show();
                    break;
            }
        });
        return apkVersion;
    }
}
