package co.coinfinity.infineonandroidapp.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import co.coinfinity.AppConstants;
import co.coinfinity.infineonandroidapp.common.ByteUtils;

import java.io.IOException;
import java.util.Arrays;

import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.common.ByteUtils.combineByteArrays;

public class NfcUtils {

    public String getPublicKey(IsoDep isoDep, int parameter) {

        final byte[] GET_PUB_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x16, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };

        byte[] response = new byte[0];
        try {
            isoDep.connect();

            response = isoDep.transceive(GET_PUB_KEY);

            isoDep.close();

        } catch (IOException e) {
            Log.e(TAG, "exception while reading pubkey via NFC ", e);
        }

        if (!checkKeyPairAvailable(response)) {
            generateKeyPair(isoDep, 0x00);
            return getPublicKey(isoDep, parameter);
        }

        String hex = ByteUtils.bytesToHex(response);
        Log.d(TAG, "response GET_PUB_KEY: " + hex);
        return hex.subSequence(2, hex.length() - 4).toString();
    }

    public byte[] signTransaction(Tag tag, int parameter, byte[] data) {

        final byte[] GEN_SIGN = {
                (byte) 0x00, // CLA Class
                (byte) 0x18, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) data.length // LC
        };

        final byte[] lastByte = {
                (byte) 0x00
        };

        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();

            final byte[] GEN_SIGN_WITH_DATA = combineByteArrays(GEN_SIGN, combineByteArrays(data, lastByte));

            Log.d(TAG, "GEN_SIGN_WITH_DATA: " + ByteUtils.bytesToHex(GEN_SIGN_WITH_DATA));
            byte[] response = isoDep.transceive(GEN_SIGN_WITH_DATA);

            isoDep.close();

            checkErrorCode(response);

            return Arrays.copyOfRange(response, 0, response.length - 2);

        } catch (Exception e) {
            Log.e(AppConstants.TAG, "exception while signing transaction via NFC ", e);
        }

        return null;
    }

    public byte[] generateKeyPair(IsoDep isoDep, int parameter) {

        final byte[] GEN_KEY_PAIR = {
                (byte) 0x00, // CLA Class
                (byte) 0x02, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Lc
        };

        byte[] response = new byte[0];
        try {
            isoDep.connect();

            Log.d(TAG, "generating new key pair via NFC");
            response = isoDep.transceive(GEN_KEY_PAIR);

            isoDep.close();

        } catch (IOException e) {
            Log.e(TAG, "exception while generating key pair via NFC ", e);
        }

        checkErrorCode(response);

        return Arrays.copyOfRange(response, 0, response.length - 2);
    }

    private boolean checkKeyPairAvailable(byte[] response) {
        final byte[] errorCode = getErrorCode(response);

        if (errorCode == null) {
            return true;
        }

        return errorCode[0] != 106 || errorCode[1] != -120;
    }

    private void checkErrorCode(byte[] response) {
        final byte[] errorCode = getErrorCode(response);
        if (errorCode != null) {
            throw new IllegalArgumentException("Error! Card respond with an error code of " + ByteUtils.bytesToHex(errorCode));
        }
    }

    private byte[] getErrorCode(byte[] response) {
        final byte[] statusCode = Arrays.copyOfRange(response, response.length - 2, response.length);
        if (statusCode.length != 2) {
            throw new IllegalArgumentException("Response from card has no status code!");
        }

        if (statusCode[0] != -112 || statusCode[1] != 0) {
            return statusCode;
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
}
