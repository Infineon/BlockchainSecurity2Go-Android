package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import co.coinfinity.infineonandroidapp.common.Utils;

import java.io.IOException;

import static co.coinfinity.AppConstants.TAG;

public class NfcUtils {

    public static String getPublicKey(IsoDep isoDep, int parameter) throws IOException {

        final byte[] GET_PUB_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x16, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };

        byte[] response = isoDep.transceive(GET_PUB_KEY);
        String hex = Utils.bytesToHex(response);
        Log.d(TAG, "response GET_PUB_KEY: " + hex);
        return hex.subSequence(0, hex.length() - 4).toString();
    }

    public static String signTransaction(Tag tag, int parameter, String data) throws IOException {

        String hex = Utils.bytesToHex(data.getBytes());
        final String dataSize = Integer.toHexString(hex.length() / 2);

        final byte[] GEN_SIGN = {
                (byte) 0x00, // CLA Class
                (byte) 0x18, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) Integer.parseInt(dataSize), // Lc
                (byte) 0x00, //Le
        };

        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();

            final byte[] GEN_SIGN_WITH_DATA = Utils.combineByteArrays(GEN_SIGN, Utils.hexStringToByteArray(hex));

            byte[] response = isoDep.transceive(GEN_SIGN_WITH_DATA);
            Log.d(TAG, "response GEN_SIGN_WITH_DATA: " + hex);

            isoDep.close();

            String signedTransaction = Utils.bytesToHex(response);
            return signedTransaction;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //    byte[] SELECT = {
//            (byte) 0x00, // CLA Class
//            (byte) 0xA4, // INS Instruction
//            (byte) 0x04, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x0D, // Length
//            (byte) 0xD2,
//            0x76,0x00,0x00,0x04,0x15,0x02,0x00,0x01,0x00,0x00,0x00,0x01 // AID
//    };
//
//    //        reflector
//    final byte[] REFLECTOR = {
//            (byte) 0x80, // CLA Class
//            (byte) 0xFF, // INS Instruction
//            (byte) 0x00, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x01, // Length
//            (byte) 0xFF,
//            (byte) 0x00,
//    };
//
//    //        get version
//    final byte[] GET_VERSION = {
//            (byte) 0x00, // CLA Class
//            (byte) 0x88, // INS Instruction
//            (byte) 0x00, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x00, // Length
//    };
//    //        create Key
//    final byte[] CREATE_KEY = {
//            (byte) 0x00, // CLA Class
//            (byte) 0x02, // INS Instruction
//            (byte) 0x01, // P1  Parameter 1
//            (byte) 0x00, // P2  Parameter 2
//            (byte) 0x00, // Length
//    };
}
