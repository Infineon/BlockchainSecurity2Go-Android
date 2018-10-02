package co.coinfinity.infineonandroidapp.infineon;

import android.util.Log;
import co.coinfinity.infineonandroidapp.infineon.apdu.*;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;

import java.io.IOException;

import static co.coinfinity.AppConstants.TAG;
import static co.coinfinity.infineonandroidapp.infineon.apdu.GenerateKeyPairKeyApdu.CURVE_INDEX_SECP256K1;
import static co.coinfinity.infineonandroidapp.infineon.apdu.ResponseApdu.SW_KEY_WITH_IDX_NOT_AVAILABLE;
import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;

/**
 * @author Johannes Zweng on 02.10.18.
 */
public class NfcUtils {

    /**
     * Read public key from card
     *
     * @param card  nfc card
     * @param keyId key to get
     * @return public key as hexadecimal String
     * @throws IOException on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static String readPublicKeyFromCard(NfcTranceiver card, int keyId)
            throws IOException, NfcCardException {
        GetPubKeyApdu apdu = new GetPubKeyApdu(keyId);

        // send apdu
        ResponseApdu resp = tranceive(card, apdu, "GET PUBLIC KEY");


        // at the moment we only support uncompressed keys
        // (identified by prefix 0x04 followed by 2x 32 bytes, x- and y- coordinate)
        if (resp.getData()[0] != (byte) 0x04 || resp.getData().length != 65) {
            throw new NfcCardException(resp.getSW1SW2(), String.format("Cannot parse returned " +
                    "PubKey from card. Expected uncompressed 64 byte long key data, prefixed with 0x04, " +
                    "but got instead: %s", bytesToHex(resp.getData())));
        }

        // get DATA part of response and convert to hex string
        String hex = bytesToHex(resp.getData());

        // cut off the first byte (2 hex characters), which contain the 0x04 (prefix for uncompressed keys)
        return hex.substring(2);
    }


    /**
     * Generate signature
     *
     * @param card       nfc card
     * @param keyIndex   index of the key to use
     * @param dataToSign data to be signed (hash)
     * @return signature data as byte array
     * @throws IOException on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static byte[] generateSignature(NfcTranceiver card, int keyIndex, byte[] dataToSign)
            throws IOException, NfcCardException {
        GenerateSignatureApdu apdu = new GenerateSignatureApdu(keyIndex, dataToSign);

        // send apdu and check response status word
        ResponseApdu resp = tranceive(card, apdu, "GET PUBLIC KEY");

        //return signature data
        return resp.getData();
    }


    /**
     * Generate a new Secp256k1 keypair on the card.
     *
     * @param card nfc tranceiver
     * @return index of the newly created key
     * @throws IOException on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static int generateNewSecp256K1Keypair(NfcTranceiver card)
            throws IOException, NfcCardException {
        GenerateKeyPairKeyApdu apdu = new GenerateKeyPairKeyApdu(CURVE_INDEX_SECP256K1);

        // send apdu and check response status word
        ResponseApdu resp = tranceive(card, apdu, "GET PUBLIC KEY");

        // should return exactly 1 byte, indicating index of new key
        if (resp.getData().length != 1) {
            throw new IllegalStateException(String.format("GENERATE KEYPAIR response was not " +
                    "exactly 1 byte long: %s", resp.getDataAsHex()));
        }
        return (int) resp.getData()[0];
    }


    /**
     * Send command APDU to card
     *
     * @param card        nfc card
     * @param commandApdu command
     * @param commandName used for error message
     * @return response
     * @throws IOException on communication errors
     * @throws NfcCardException if card reponse status words are != 0x9000
     */
    private static ResponseApdu tranceive(NfcTranceiver card, BaseCommandApdu commandApdu, String commandName)
            throws IOException, NfcCardException {
        Log.d(TAG, String.format("CMD: %s - APDU SENT: >>> %s", commandName, commandApdu.toHexString()));
        ResponseApdu responseApdu = new ResponseApdu(card.transceive(commandApdu.toBytes()));
        Log.d(TAG, String.format("CMD: %s - APDU RCVD: <<< %s", commandName, responseApdu.toHexString()));

        // check if Status OK
        if (!responseApdu.isSuccess()) {
            throw new NfcCardException(responseApdu.getSW1SW2(),
                    String.format("Sending %s failed with response status 0x%s",
                            commandName, responseApdu.getSW1SW2HexString()));
        }

        // return on success
        return responseApdu;
    }

    /**
     * Read public key from card, or create a new one if it doesn't exist yet
     *
     * @param card nfc card
     * @return public key as hexadecimal String
     * @throws IOException on communication errors
     * @throws NfcCardException when card returns something other than 0x9000
     */
    public static String readPublicKeyOrCreateIfNotExists(NfcTranceiver card)
            throws IOException, NfcCardException {

        int keyIdx = 0x00; // we use default key index

        try {
            // try to read public key
            return readPublicKeyFromCard(card, keyIdx);
        } catch (NfcCardException e) {
            // if Public key is not available yet (Status words: 0x6A88)
            if (e.getSw1Sw2() == SW_KEY_WITH_IDX_NOT_AVAILABLE) {
                // create a new keypair
                int newKeyIndex = generateNewSecp256K1Keypair(card);
                // and ask for the pubkey of the newly created keypair
                return readPublicKeyFromCard(card, newKeyIndex);
            } else {
                throw e;
            }
        }
    }


}
