package co.coinfinity.infineonandroidapp.infineon;

import android.util.Log;
import co.coinfinity.infineonandroidapp.infineon.apdu.*;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GenerateSignatureResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GetKeyInfoResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.ResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.exceptions.ExceptionHandler;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;

import java.io.IOException;

import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.infineon.apdu.GenerateKeyPairApdu.CURVE_INDEX_SECP256K1;
import static co.coinfinity.infineonandroidapp.infineon.apdu.response.ResponseApdu.SW_KEY_WITH_IDX_NOT_AVAILABLE;
import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;
import static co.coinfinity.infineonandroidapp.utils.ByteUtils.fromHexString;

/**
 * Utils class used to interact with the Infineon card via NFC.
 *
 * @author Johannes Zweng on 02.10.18.
 */
public class NfcUtils {

    /**
     * AID of com.ifx.javacard.applets.blockchain.Security2Go
     */
    public static final byte[] AID_INFINEON_BLOCKCHAIN2GO = fromHexString("D2760000041502000100000001");


    /**
     * Generate an ECDSA signature.
     *
     * @param card       nfc card
     * @param keyIndex   index of the key to use
     * @param dataToSign data to be signed (hash)
     * @return signature data as byte array
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static GenerateSignatureResponseApdu generateSignature(NfcTranceiver card, int keyIndex,
                                                                  byte[] dataToSign, byte[] pin)
            throws IOException, NfcCardException {
        selectApplication(card);

        if (pin != null && pin.length > 0) {
            if (!verifyPin(card, pin)) {
                return new GenerateSignatureResponseApdu(new byte[]{});
            }
        }

        GenerateSignatureApdu apdu = new GenerateSignatureApdu(keyIndex, dataToSign);
        return (GenerateSignatureResponseApdu) tranceive(card, apdu, "GENERATE SIGNATURE");
    }

    /**
     * Read public key from card, or create a new one if it doesn't exist yet.
     *
     * @param card nfc card
     * @return public key as hexadecimal String
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static GetKeyInfoResponseApdu readPublicKeyOrCreateIfNotExists(NfcTranceiver card, int keyIndex)
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

    /**
     * Creates a new key on index 0 within the card, derived by the given seed.
     * Giving the same seed will always create the same key on the card.
     *
     * @param card nfc tranceiver
     * @param seed key derivation seed
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static boolean generateKeyFromSeed(NfcTranceiver card, byte[] seed) throws IOException, NfcCardException {
        selectApplication(card);

        GenerateKeyFromSeedApdu apdu = new GenerateKeyFromSeedApdu(seed);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "GENERATE KEY FROM SEED");
        return resp.getSW1() == 0x90;
    }


    /**
     * Initial set up of the PIN. This is only allowed in the PIN inactive state.
     *
     * @param card     nfc tranceiver
     * @param pinBytes new pin to set (any byte array with length between 4 and 63 bytes is allowed)
     * @return PUK bytes
     * @throws IOException              on communication errors
     * @throws IllegalArgumentException if given PIN is null or shorter than 4 bytes or longer than 63 bytes
     * @throws NfcCardException         when card returns something other than 0x9000 or 0x61XX
     */
    public static byte[] initializePinAndReturnPuk(NfcTranceiver card, byte[] pinBytes) throws IOException, NfcCardException {
        selectApplication(card);

        if (pinBytes == null || pinBytes.length < 4 || pinBytes.length > 63) {
            throw new IllegalArgumentException("PIN must be a byte array between 4 and 63 bytes length");
        }

        SetPinApdu apdu = new SetPinApdu(pinBytes);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "SET PIN");

        return resp.getData();
    }

    /**
     * Change current PIN to a new PIN. This is only allowed in the PIN active state.
     *
     * @param card       nfc tranceiver
     * @param currentPin current PIN
     * @param newPin     new PIN
     * @return PUK bytes
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static byte[] changePin(NfcTranceiver card, byte[] currentPin, byte[] newPin) throws IOException, NfcCardException {
        selectApplication(card);

        if (currentPin != null && currentPin.length > 0) {
            if (!verifyPin(card, currentPin)) {
                return new byte[]{};
            }
        }

        ChangePinApdu apdu = new ChangePinApdu(currentPin, newPin);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "CHANGE PIN");

        return resp.getData();
    }

    /**
     * Unlock PIN with PUK to get back into PIN inactive state.
     *
     * @param card nfc tranceiver
     * @param puk  PUK bytes
     * @return if unlocking worked or not
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static boolean unlockPin(NfcTranceiver card, byte[] puk) throws IOException, NfcCardException {
        selectApplication(card);

        UnlockPinApdu apdu = new UnlockPinApdu(puk);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "UNLOCK PIN");
        return resp.getSW1() == 0x90;
    }

    /**
     * Verify PIN if correct or not and return as boolean.
     *
     * @param card nfc tranceiver
     * @param pin  PIN to verify
     * @return if verified or not
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    public static boolean verifyPin(NfcTranceiver card, byte[] pin) throws IOException, NfcCardException {
        VerifyPinApdu apdu = new VerifyPinApdu(pin);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "VERIFY PIN");
        return resp.getSW1() == 0x90;
    }

    /**
     * Select the application of Infineon Blockchain2go
     *
     * @param card nfc tranceiver
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    private static void selectApplication(NfcTranceiver card) throws IOException, NfcCardException {
        SelectApplicationApdu apdu = new SelectApplicationApdu(AID_INFINEON_BLOCKCHAIN2GO);
        // send apdu
        tranceive(card, apdu, "SELECT APPLICATION");
    }

    /**
     * Read public key from card.
     *
     * @param card  nfc card
     * @param keyId key to get
     * @return public key as hexadecimal String
     * @throws IOException      on communication errors
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
     */
    private static GetKeyInfoResponseApdu readPublicKeyFromCard(NfcTranceiver card, int keyId)
            throws IOException, NfcCardException {
        GetKeyInfoApdu apdu = new GetKeyInfoApdu(keyId);

        // send apdu
        GetKeyInfoResponseApdu resp = (GetKeyInfoResponseApdu) tranceive(card, apdu, "GET KEY INFO");

        // at the moment we only support uncompressed keys
        // (identified by prefix 0x04 followed by 2x 32 bytes, x- and y- coordinate)
        if (resp.getData()[8] != (byte) 0x04 || resp.getData().length != 73) {
            throw new NfcCardException(resp.getSW1SW2(), String.format("Cannot parse returned " +
                    "PubKey from card. Expected uncompressed 64 byte long key data, prefixed with 0x04, " +
                    "but got instead: %s", bytesToHex(resp.getData())));
        }
        return resp;
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

        ResponseApdu responseApdu;
        if (commandApdu instanceof GetKeyInfoApdu) {
            responseApdu = new GetKeyInfoResponseApdu(card.transceive(commandApdu.toBytes()));
        } else if (commandApdu instanceof GenerateSignatureApdu) {
            responseApdu = new GenerateSignatureResponseApdu(card.transceive(commandApdu.toBytes()));
        } else {
            responseApdu = new ResponseApdu(card.transceive(commandApdu.toBytes()));
        }

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
     * @throws NfcCardException when card returns something other than 0x9000 or 0x61XX
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
