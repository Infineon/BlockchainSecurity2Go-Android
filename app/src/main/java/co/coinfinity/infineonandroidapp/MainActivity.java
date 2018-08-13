package co.coinfinity.infineonandroidapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import co.coinfinity.infineonandroidapp.nfc.NdefMessageParser;
import co.coinfinity.infineonandroidapp.nfc.record.ParsedNdefRecord;

import java.io.IOException;
import java.util.List;

import static co.coinfinity.infineonandroidapp.nfc.DumpTagData.dumpTagData;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        byte[] SELECT = {
                (byte) 0x00, // CLA Class
                (byte) 0xA4, // INS Instruction
                (byte) 0x04, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x0A, // Length
                0x63,0x64,0x63,0x00,0x00,0x00,0x00,0x32,0x32,0x31 // AID
        };

        //        reflector
        final byte[] REFLECTOR = {
                (byte) 0x80, // CLA Class
                (byte) 0xFF, // INS Instruction
                (byte) 0x00, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x01, // Length
                (byte) 0xFF,
                (byte) 0x00,
        };

        //        get pub key
        final byte[] GET_PUB_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x16, // INS Instruction
                (byte) 0x00, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };

        //        get version
        final byte[] GET_VERSION = {
                (byte) 0x00, // CLA Class
                (byte) 0x88, // INS Instruction
                (byte) 0x00, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };
        //        create Key
        final byte[] CREATE_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x02, // INS Instruction
                (byte) 0x01, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d("TAG", "Tag found: " + tagFromIntent.toString());
        Log.d("TAG", "Id: " + bytesToHex(tagFromIntent.getId()));
        for (String tech: tagFromIntent.getTechList()) {
            Log.d("TAG", "Tech: " + tech);
        }

        IsoDep isoDep = IsoDep.get(tagFromIntent);
        try {
            isoDep.connect();

//            byte[] result = isoDep.transceive(SELECT);
////            if (!(result[0] == (byte) 0x90 && result[1] == (byte) 0x00))
////                throw new IOException("could not select applet");
//
//            String str = bytesToHex(result);
//            text.setText(str);

            byte[] result2 = isoDep.transceive(CREATE_KEY);
            String str2 = bytesToHex(result2);
            text.setText(text.getText()+" "+str2);

//            byte[] result3 = isoDep.transceive(GET_PUB_KEY);
//            String str3 = bytesToHex(result3);
//            text.setText(text.getText()+" "+str3);

            isoDep.close();
        } catch (Exception e) {
            String error = e.getMessage();
        }
//
//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage[] msgs;
//
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                }
//
//            } else {
//                byte[] empty = new byte[0];
//                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
//                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//                byte[] payload = dumpTagData(tag).getBytes();
//                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
//                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
//                msgs = new NdefMessage[] {msg};
//            }
//
//            displayMsgs(msgs);
//        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void displayMsgs(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        text.setText(builder.toString());
    }
}
