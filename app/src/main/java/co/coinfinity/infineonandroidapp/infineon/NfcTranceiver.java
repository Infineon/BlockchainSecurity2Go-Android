package co.coinfinity.infineonandroidapp.infineon;

import java.io.IOException;

/**
 * Interface for sending and receiving APDU commands to a card.
 * Abstracts Android specific NFC reader stuff away into this generic interface.
 *
 * @author Coinfinity.co 2018
 */
public interface NfcTranceiver {

    /**
     * Sends a command APDU to the NFC card and returns the received response APDU
     *
     * @param commandApdu command APDU to send
     * @return bytes reveived as response
     * @throws IOException in case of communication errors
     */
    byte[] transceive(byte[] commandApdu) throws IOException;
}
