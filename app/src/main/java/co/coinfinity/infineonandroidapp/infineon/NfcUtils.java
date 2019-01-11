package co.coinfinity.infineonandroidapp.infineon;

import android.util.Log;
import co.coinfinity.infineonandroidapp.infineon.apdu.*;
import co.coinfinity.infineonandroidapp.infineon.exceptions.ExceptionHandler;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.infineon.apdu.GenerateKeyPairApdu.CURVE_INDEX_SECP256K1;
import static co.coinfinity.infineonandroidapp.infineon.apdu.ResponseApdu.SW_KEY_WITH_IDX_NOT_AVAILABLE;
import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;

/**
 * Utils class used to interact with the Infineon card via NFC.
 *
 * @author Johannes Zweng on 02.10.18.
 */
public class NfcUtils {

    /**
     * Generate an ECDSA signature.
     *
     * @param card       nfc card
     * @param keyIndex   index of the key to use
     * @param dataToSign data to be signed (hash)
     * @return signature data as byte array
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static byte[] generateSignature(NfcTranceiver card, int keyIndex, byte[] dataToSign, String pin)
            throws IOException, NfcCardException {
        selectApplication(card);

        if (pin != null && !pin.isEmpty()) {
            verifyPin(card, pin);
        }

        GenerateSignatureApdu apdu = new GenerateSignatureApdu(keyIndex, dataToSign);

        // send apdu and check response status word
        ResponseApdu resp = tranceive(card, apdu, "GENERATE SIGNATURE");

        //return signature data and remove first 8 bytes
        return Arrays.copyOfRange(resp.getData(), 8, resp.getData().length);
    }

    /**
     * Read public key from card, or create a new one if it doesn't exist yet.
     *
     * @param card nfc card
     * @return public key as hexadecimal String
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static String readPublicKeyOrCreateIfNotExists(NfcTranceiver card, int keyIndex)
            throws IOException, NfcCardException {
        try {
            selectApplication(card);
            // try to read public key
            return readPublicKeyFromCard(card, keyIndex);
        } catch (NfcCardException e) {
            // if Public key is not available yet (Status words: 0x6A88)
            if (e.getSw1Sw2() == SW_KEY_WITH_IDX_NOT_AVAILABLE) {
                int newKeyIndex;
                do {
                    // create a new keypair
                    newKeyIndex = generateNewSecp256K1Keypair(card);
                } while (newKeyIndex <= keyIndex);
                // and ask for the pubkey of the newly created keypair
                return readPublicKeyFromCard(card, newKeyIndex);
            } else {
                // throw all other exceptions to our caller
                throw e;
            }
        }
    }

    public static void generateKeyFromSeed(NfcTranceiver card, String seed) throws IOException, NfcCardException {
        selectApplication(card);

        GenerateKeyFromSeedApdu apdu = new GenerateKeyFromSeedApdu(seed.getBytes());

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "GENERATE KEY FROM SEED");
        //TODO what to return here? boolean?
    }

    public static String initializePinAndReturnPuk(NfcTranceiver card, String pin) throws IOException, NfcCardException {
        selectApplication(card);

        SetPinApdu apdu = new SetPinApdu(pin.getBytes());

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "SET PIN");

        //TODO fix this later on - should return puk
        // get DATA part of response and convert to hex string
        return new String(resp.getData(), 0, 8, Charset.defaultCharset());
    }

    public static String changePin(NfcTranceiver card, String currentPin, String newPin) throws IOException, NfcCardException {
        selectApplication(card);

        ChangePinApdu apdu = new ChangePinApdu(currentPin.getBytes(), newPin.getBytes());

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "CHANGE PIN");

        //TODO fix this later on - should return puk
        // get DATA part of response and convert to hex string
        return new String(resp.getData(), 0, 8, Charset.defaultCharset());
    }

    public static void unlockPin(NfcTranceiver card, String puk) throws IOException, NfcCardException {
        selectApplication(card);

        UnlockPinApdu apdu = new UnlockPinApdu(puk.getBytes());

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "UNLOCK PIN");
        //TODO what to do with response?
    }

    public static void verifyPin(NfcTranceiver card, String pin) throws IOException, NfcCardException {
        VerifyPinApdu apdu = new VerifyPinApdu(pin.getBytes());

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "VERIFY PIN");
        //TODO what to do with response?
    }

    private static void selectApplication(NfcTranceiver card) throws IOException, NfcCardException {
        SelectApplicationApdu apdu = new SelectApplicationApdu();

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "SELECT APPLICATION");
        //TODO what to do with response?
    }

    //TODO TESTT !!!!
    public static void resetCard(NfcTranceiver card) throws IOException, NfcCardException {
        selectApplication(card);

        ResetApdu apdu = new ResetApdu();

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "RESET CARD");
    }

    /**
     * Read public key from card.
     *
     * @param card  nfc card
     * @param keyId key to get
     * @return public key as hexadecimal String
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    private static String readPublicKeyFromCard(NfcTranceiver card, int keyId)
            throws IOException, NfcCardException {
        GetKeyInfoApdu apdu = new GetKeyInfoApdu(keyId);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "GET KEY INFO");

        // at the moment we only support uncompressed keys
        // (identified by prefix 0x04 followed by 2x 32 bytes, x- and y- coordinate)
        //TODO do something with glob sig and sig count currently we just cut it off
        if (resp.getData()[8] != (byte) 0x04 || resp.getData().length != 73) {
            throw new NfcCardException(resp.getSW1SW2(), String.format("Cannot parse returned " +
                    "PubKey from card. Expected uncompressed 64 byte long key data, prefixed with 0x04, " +
                    "but got instead: %s", bytesToHex(resp.getData())));
        }

        // get DATA part of response and convert to hex string
        String hex = bytesToHex(resp.getData());

        // cut off the first bytes, which contain global sig + sig count + 0x04 (prefix for uncompressed keys)
        return hex.substring(18);
    }

    /**
     * Send command APDU to card.
     *
     * @param card        nfc card
     * @param commandApdu command
     * @param commandName used for error message
     * @return response
     * @throws IOException      on communication errors
     * @throws NfcCardException if card reponse status words are != 0x9000
     */
    private static ResponseApdu tranceive(NfcTranceiver card, BaseCommandApdu commandApdu, String commandName)
            throws IOException, NfcCardException {
        Log.d(TAG, String.format("CMD: %s - APDU SENT: >>> %s", commandName, commandApdu.toHexString()));
        ResponseApdu responseApdu = new ResponseApdu(card.transceive(commandApdu.toBytes()));
        Log.d(TAG, String.format("CMD: %s - APDU RCVD: <<< %s", commandName, responseApdu.toHexString()));

        // check if Status OK
        if (!responseApdu.isSuccess()) {
            ExceptionHandler.handleErrorCodes(commandApdu, responseApdu.getSW1SW2());
        }

        // return on success
        return responseApdu;
    }

    /**
     * Generate a new SECP-256k1 keypair on the card.
     *
     * @param card nfc tranceiver
     * @return index of the newly created key
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    private static int generateNewSecp256K1Keypair(NfcTranceiver card)
            throws IOException, NfcCardException {
        GenerateKeyPairApdu apdu = new GenerateKeyPairApdu(CURVE_INDEX_SECP256K1);

        // send apdu and check response status word
        ResponseApdu resp = tranceive(card, apdu, "GENERATE KEYPAIR");

        // should return exactly 1 byte, indicating index of new key
        if (resp.getData().length != 1) {
            throw new IllegalStateException(String.format("GENERATE KEYPAIR response was not " +
                    "exactly 1 byte long: %s", resp.getDataAsHex()));
        }
        return (int) resp.getData()[0];
    }


}
