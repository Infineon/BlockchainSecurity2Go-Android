package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.tech.IsoDep;

import java.io.IOException;

public class NfcUtils {

    public static String getPublicKey(IsoDep isoDep, int parameter) throws IOException {
        //        get pub key
        final byte[] GET_PUB_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x16, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };

        byte[] response = isoDep.transceive(GET_PUB_KEY);
        String hex = bytesToHex(response);
        return hex.subSequence(0,hex.length()-4).toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
