package co.coinfinity.infineonandroidapp.qrcode;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

public class QrCodeScanner {

    public static void scanQrCode(AppCompatActivity appCompatActivity) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            appCompatActivity.startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            appCompatActivity.startActivity(marketIntent);
        }
    }
}
