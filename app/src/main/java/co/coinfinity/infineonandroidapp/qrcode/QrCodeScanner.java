package co.coinfinity.infineonandroidapp.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class QrCodeScanner {

    /**
     * Open external QR Code scanner via Intent
     *
     * @param activity calling activity
     */
    public static void scanQrCode(Activity activity, int requestCode) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            // redirect to Market if not installed
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            activity.startActivity(marketIntent);
        }
    }
}
